package com.example.gon;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.util.*;

public class FriendAdaptor extends RecyclerView.Adapter<FriendAdaptor.FriendViewHolder> {
    private List<Friend> friendsList;

            public FriendAdaptor(List<Friend> friendsList){
        this.friendsList = friendsList;}


    //The following functions need to be overriden for RecyclerView.Adapter to work with our friend_card.xml
    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Overrides onCreateViewHolder to use our friend_card XML layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_card, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
      Friend friend = friendsList.get(position);
      holder.tvFriendUsername.setText(friend.getUsername());


      holder.btnViewGoals.setOnClickListener(v -> {
          Toast.makeText(v.getContext(), "hello", Toast.LENGTH_LONG).show();
      });
//
//        holder.btnNudge.setOnClickListener(v -> {
//            // do later
//        });
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder{

        TextView tvFriendUsername;
        Button btnViewGoals;
        Button btnNudge;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
           tvFriendUsername = itemView.findViewById(R.id.tvFriendUsername);
           btnNudge = itemView.findViewById(R.id.btnNudge);
           btnViewGoals = itemView.findViewById(R.id.btnViewGoals);


        }

    }
}





