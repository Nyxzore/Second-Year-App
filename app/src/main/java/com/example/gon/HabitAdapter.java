package com.example.gon;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
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
        String desc = h.getDescription() != null ? h.getDescription().trim() : "";
        if (desc.isEmpty()) {
            holder.txtStatus.setText(h.isCompletedToday() ? "Completed today ✓" : "Swipe to complete");
        } else {
            holder.txtStatus.setText(h.isCompletedToday() ? "Completed today ✓" : desc);
        }
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public static class HabitViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView txtTitle;
        TextView txtStatus;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtHabitTitle);
            txtStatus = itemView.findViewById(R.id.txtCompletionStatus);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int pos = getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            menu.add(pos, 101, 0, "Edit");
            menu.add(pos, 102, 1, "Delete");
        }
    }
}