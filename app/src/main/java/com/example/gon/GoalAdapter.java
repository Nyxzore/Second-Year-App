package com.example.gon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    private List<Goal> goal_list; //to be refactored to use goal object

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

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.goal_card, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal currentGoal = goal_list.get(position);
        holder.textViewGoalName.setText(currentGoal.getTitle());
        holder.textViewGoalDescription.setText(currentGoal.getDescription());
        holder.textViewGoalDate.setText("Plan to do by " + currentGoal.getDueDate());
    }

    @Override
    public int getItemCount() {
        return goal_list.size();
    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView textViewGoalName;
        TextView textViewGoalDescription;
        TextView textViewGoalDate;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGoalName = itemView.findViewById(R.id.textViewGoalName);
            textViewGoalDescription = itemView.findViewById(R.id.textViewGoalDescription);
            textViewGoalDate = itemView.findViewById(R.id.textViewGoalDate);
        }
    }
}
