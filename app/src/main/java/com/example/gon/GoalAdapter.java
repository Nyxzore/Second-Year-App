package com.example.gon;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


/*
This class is need to call recyclerView.setAdapter(adapter); in GoalList.java
This class tells the RecyclerView how to handle generating our goal cards
 */
public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    private List<Goal> goal_list; //goals you wish to load

    public List<Goal> getGoal_list() {
        return goal_list;
    }

    public GoalAdapter(List<Goal> goal_list){
        this.goal_list = goal_list;
    }

    public GoalAdapter(){
        this.goal_list = new ArrayList<>();
        this.goal_list.add(new Goal());
    }

    //The following functions need to be overriden for RecyclerView.Adapter to work with our goal_card.xml
    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Overrides onCreateViewHolder to use our goal_card XML layout which consists of 3 textviews
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.goal_card, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal currentGoal = goal_list.get(position);
        holder.textViewGoalName.setText(currentGoal.getTitle());
        holder.textViewGoalDescription.setText(currentGoal.getDescription());
        holder.textViewGoalDate.setText("Plan to do by " + currentGoal.getDueDate());

        try {
            Date due_date = currentGoal.getDueDateAsDate();
            Date today = new Date();
            if (today.after(due_date)) {
                holder.textViewStatus.setText("Overdue");
                holder.textViewStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.overdue_date));
            } else {
                holder.textViewStatus.setText("Active");
                holder.textViewStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.ontrack_date));
            }
        } catch (ParseException e) {
            holder.textViewStatus.setText("Active");
            holder.textViewStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.ontrack_date));
        }
    }

    @Override
    public int getItemCount() {
        return goal_list.size();
    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        //This "holds" our goals its essentially just what is inside the card
        //Used 3 textviews to store simple data but plan to maybe include images and a few more icons to edit, delete the goal
        TextView textViewGoalName;
        TextView textViewGoalDescription;
        TextView textViewGoalDate;
        TextView textViewStatus;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGoalName = itemView.findViewById(R.id.textViewGoalName);
            textViewGoalDescription = itemView.findViewById(R.id.textViewGoalDescription);
            textViewGoalDate = itemView.findViewById(R.id.textViewGoalDate);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);

            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(this.getBindingAdapterPosition(), 101, 0, "Edit");
            menu.add(this.getBindingAdapterPosition(), 102, 1, "Delete");
        }
    }
}
