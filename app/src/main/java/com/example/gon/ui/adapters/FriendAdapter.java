package com.example.gon.ui.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gon.Entities.Friend;
import com.example.gon.ui.activities.FriendGoalsActivity;
import com.example.gon.R;
import com.example.gon.utils.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class FriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Friend> friendsList;
    private static final int T_FRIEND = 1;
    private static final int T_REQUEST = 2;
            public FriendAdapter(List<Friend> friendsList){
        this.friendsList = friendsList;}




    @Override    //gets the correct card to use
    public int getItemViewType(int position) {
        Friend friend = friendsList.get(position);

        if (friend.getStatus().equalsIgnoreCase("Pending")) {
            return T_REQUEST;
        } else {
            return T_FRIEND;
        }
    }

    //The following functions need to be overriden for RecyclerView.Adapter to work with our friend_card.xml
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Overrides onCreateViewHolder to use our friend_card XML layout
        if (viewType == T_FRIEND){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_card, parent, false);
            return new FriendViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.freindreq_card, parent, false);
            return new FriendReqViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Friend friend = friendsList.get(position);

        if (holder instanceof FriendViewHolder) {
            FriendViewHolder friendHolder = (FriendViewHolder) holder;

            friendHolder.tvFriendUsername.setText(friend.getUsername());

            friendHolder.btnViewGoals.setOnClickListener(v -> {
                viewFriendGoals(v,friend);
            });

            friendHolder.btnNudge.setOnClickListener(v -> {

                //TO DO LAST
                nudgeFriend(v,friend);
            });

        } else if (holder instanceof FriendReqViewHolder) {
            FriendReqViewHolder requestHolder = (FriendReqViewHolder) holder;

            requestHolder.tvUsername.setText(friend.getUsername());

            requestHolder.btnAccept.setOnClickListener(v -> {
                acceptFriendRequest(v,friend,holder.getAdapterPosition());  //send holder postition to change the recycler view
            });

            requestHolder.btnIgnore.setOnClickListener(v -> {
                ignoreFriendRequest(v,friend,holder.getAdapterPosition());
//                int curPos = holder.getAdapterPosition();  //gets current card clicked
//
//                if (curPos != RecyclerView.NO_POSITION){
//                    Toast.makeText(v.getContext(), "Ignored " + friend.getUsername(), Toast.LENGTH_LONG).show(); //checks trhat it exists
//THIS CODE IS COMMENTED OUT AS ITS PUT LATER IN THE FUCNTION
//                    friendsList.remove(curPos); //removes from friendslist
//                    notifyItemRemoved(curPos);  //removes from recycler view
//                    notifyItemRangeChanged(curPos,friendsList.size()); //refreshes other cards

            });
        }
    }
   //START OF FUNCTIONS FOR FRIENDS   STILL THE SHELL TO DO!!!!
    private void viewFriendGoals(View v, Friend friend) {
        // TO DO:
        // Later this should open a FriendGoalsList activity
        // and pass friend.getUserID() to load that friend's goals.

        // Nickson's changes
        Intent intent = new Intent(v.getContext(), FriendGoalsActivity.class);
        intent.putExtra("FRIEND_ID", friend.getUserID());
        intent.putExtra("FRIEND_NAME", friend.getUsername());
        v.getContext().startActivity(intent);


        //
    }

    private void nudgeFriend(View v, Friend friend) {
        // TO DO:
        // Later this should call nudge_friend.php
        // with the logged-in user's ID and this friend's userID.

        Toast.makeText(
                v.getContext(),
                "Nudged " + friend.getUsername(),
                Toast.LENGTH_SHORT
        ).show();
    }

    private void acceptFriendRequest(View v, Friend friend, int position) {
        // TO DO:
        // Later this should call accept_friend.php
        // and update the request's status to Accepted in the database.

        if (position != RecyclerView.NO_POSITION) {
            Map<String, String> params = new HashMap<>();
            params.put("uuid", PreferenceManager.get_uuid(v.getContext()));
            params.put("friend_id", friend.getUserID());

            PreferenceManager.post("accept_friend.php", params, response -> {
                ((AppCompatActivity) v.getContext()).runOnUiThread(() -> {
                    friendsList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, friendsList.size());

                    Toast.makeText(v.getContext(), "Accepted " + friend.getUsername(), Toast.LENGTH_SHORT).show();
                });
            });
            // For now, remove request locally from RecyclerView
        }
    }

    private void ignoreFriendRequest(View v, Friend friend, int position) {
        // TO DO:
        // Later this should call ignore_friend.php
        // and either delete the request or mark it as Ignored in the database.

        if (position != RecyclerView.NO_POSITION) {
            Map<String, String> params = new HashMap<>();
            params.put("uuid", PreferenceManager.get_uuid(v.getContext()));
            params.put("friend_id", friend.getUserID());

            PreferenceManager.post("ignore_friend.php", params, response -> {
                ((AppCompatActivity) v.getContext()).runOnUiThread(() -> {
                    friendsList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, friendsList.size());

                    Toast.makeText(v.getContext(), "Ignored request", Toast.LENGTH_SHORT).show();
                });
            });
        }
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder{  //FRIEND CARD SETUP

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
    public static class FriendReqViewHolder extends RecyclerView.ViewHolder {  //FRIEND REQ CARD SETUP

        TextView tvUsername;
        Button btnAccept;
        Button btnIgnore;

        public FriendReqViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.tvUsername);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnIgnore = itemView.findViewById(R.id.btnIgnore);
        }
    }



}





