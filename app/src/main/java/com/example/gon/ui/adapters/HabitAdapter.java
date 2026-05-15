package com.example.gon.ui.adapters;

import android.graphics.Color;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gon.R;
import com.example.gon.Entities.Habit;
import com.example.gon.ui.helpers.CategoryUiHelper;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {
    private final List<Habit> habit_list;

    public HabitAdapter(List<Habit> habit_list) {
        this.habit_list = habit_list;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int view_type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit h = habit_list.get(position);
        holder.txt_title.setText(h.get_name());

        String desc = h.get_description();
        if (desc != null && !desc.isEmpty()) {
            holder.txt_description.setText(desc);
            holder.txt_description.setVisibility(View.VISIBLE);
        } else {
            holder.txt_description.setVisibility(View.GONE);
        }

        CategoryUiHelper.bind_display_chips(holder.itemView.getContext(),
                holder.chip_group_habit_categories, h.get_categories());

        if (h.is_completed_today()) {
            holder.txt_status.setText("Completed ✓");
            holder.txt_status.setTextColor(Color.GRAY);
            holder.itemView.setAlpha(0.5f);
        } else {
            holder.txt_status.setText("Swipe to complete");
            holder.txt_status.setTextColor(Color.parseColor("#4B8A5B"));
            holder.itemView.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() { return habit_list.size(); }

    public static class HabitViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView txt_title, txt_description, txt_status;
        ChipGroup chip_group_habit_categories;

        public HabitViewHolder(@NonNull View item_view) {
            super(item_view);
            txt_title = item_view.findViewById(R.id.txtHabitTitle);
            txt_description = item_view.findViewById(R.id.txtHabitDescription);
            txt_status = item_view.findViewById(R.id.txtCompletionStatus);
            chip_group_habit_categories = item_view.findViewById(R.id.chipGroupHabitCategories);
            item_view.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menu_info) {
            int pos = getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                menu.add(pos, 101, 0, "Edit");
                menu.add(pos, 102, 1, "Delete");
            }
        }
    }
}
