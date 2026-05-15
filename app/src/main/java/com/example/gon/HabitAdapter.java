package com.example.gon;

import android.graphics.Color;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {
    private final List<Habit> habitList;

    public HabitAdapter(List<Habit> habitList) {
        this.habitList = habitList;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.habit_card, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit h = habitList.get(position);
        holder.txtTitle.setText(h.getName());

        String desc = h.getDescription();
        if (desc != null && !desc.isEmpty()) {
            holder.txtDescription.setText(desc);
            holder.txtDescription.setVisibility(View.VISIBLE);
        } else {
            holder.txtDescription.setVisibility(View.GONE);
        }

        String categoryLabel = Category.joinNames(h.getCategories());
        if (categoryLabel.isEmpty()) {
            holder.txtCategories.setVisibility(View.GONE);
        } else {
            holder.txtCategories.setVisibility(View.VISIBLE);
            holder.txtCategories.setText(categoryLabel);
        }

        if (h.isCompletedToday()) {
            holder.txtStatus.setText("Completed ✓");
            holder.txtStatus.setTextColor(Color.GRAY);
            holder.itemView.setAlpha(0.5f);
        } else {
            holder.txtStatus.setText("Swipe to complete");
            holder.txtStatus.setTextColor(Color.parseColor("#4B8A5B"));
            holder.itemView.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() { return habitList.size(); }

    public static class HabitViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView txtTitle, txtDescription, txtStatus, txtCategories;
        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtHabitTitle);
            txtDescription = itemView.findViewById(R.id.txtHabitDescription);
            txtStatus = itemView.findViewById(R.id.txtCompletionStatus);
            txtCategories = itemView.findViewById(R.id.txtHabitCategories);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int pos = getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                menu.add(pos, 101, 0, "Edit");
                menu.add(pos, 102, 1, "Delete");
            }
        }
    }
}