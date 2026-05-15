package com.example.gon.ui.adapters;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.chip.ChipGroup;

import com.example.gon.R;
import com.example.gon.Entities.Goal;
import com.example.gon.ui.helpers.CategoryUiHelper;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int type_header = 0;
    private static final int type_item = 1;
    private static final int type_footer = 2;

    private final List<Goal> goal_list;
    private HeaderViewHolder header_view_holder;
    private HeaderBindListener header_bind_listener;

    public interface HeaderBindListener {
        void on_bind_header(HeaderViewHolder holder);
    }

    public GoalAdapter(List<Goal> goal_list) {
        this.goal_list = goal_list;
    }

    public void set_header_bind_listener(HeaderBindListener listener) {
        this.header_bind_listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return type_header;
        if (position == goal_list.size() + 1) return type_footer;
        return type_item;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int view_type) {
        if (view_type == type_header) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_goal_list, parent, false);
            header_view_holder = new HeaderViewHolder(view);
            return header_view_holder;
        } else if (view_type == type_footer) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_goal_list, parent, false);
            return new FooterViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal, parent, false);
            return new GoalViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            header_view_holder = (HeaderViewHolder) holder;
            if (header_bind_listener != null) {
                header_bind_listener.on_bind_header((HeaderViewHolder) holder);
            }
        } else if (holder instanceof GoalViewHolder) {
            Goal current_goal = goal_list.get(position - 1);
            GoalViewHolder g_holder = (GoalViewHolder) holder;
            g_holder.textViewGoalName.setText(current_goal.get_title());
            g_holder.textViewGoalDescription.setText(current_goal.get_description());
            g_holder.textViewGoalDate.setText(holder.itemView.getContext().getString(R.string.plan_to_do_by, current_goal.get_due_date()));

            try {
                Date due_date = current_goal.get_due_date_as_date();
                Date today = new Date();
                if (today.after(due_date)) {
                    g_holder.textViewStatus.setText(R.string.status_overdue);
                    g_holder.textViewStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.overdue_date));
                } else {
                    g_holder.textViewStatus.setText(R.string.status_active);
                    g_holder.textViewStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.ontrack_date));
                }
            } catch (ParseException e) {
                g_holder.textViewStatus.setText(R.string.status_active);
                g_holder.textViewStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.ontrack_date));
            }

            CategoryUiHelper.bind_display_chips(holder.itemView.getContext(),
                    g_holder.chipGroupGoalCategories, current_goal.get_categories());
        }
    }

    @Override
    public int getItemCount() {
        return goal_list.size() + 2;
    }

    public HeaderViewHolder get_header_view_holder() {
        return header_view_holder;
    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView textViewGoalName;
        TextView textViewGoalDescription;
        TextView textViewGoalDate;
        TextView textViewStatus;
        ChipGroup chipGroupGoalCategories;

        public GoalViewHolder(@NonNull View item_view) {
            super(item_view);
            textViewGoalName = item_view.findViewById(R.id.textViewGoalName);
            textViewGoalDescription = item_view.findViewById(R.id.textViewGoalDescription);
            textViewGoalDate = item_view.findViewById(R.id.textViewGoalDate);
            textViewStatus = item_view.findViewById(R.id.textViewStatus);
            chipGroupGoalCategories = item_view.findViewById(R.id.chipGroupGoalCategories);

            item_view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menu_info) {
            menu.add(this.getBindingAdapterPosition(), 101, 0, "Edit");
            menu.add(this.getBindingAdapterPosition(), 102, 1, "Delete");
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView lblActiveGoals;
        public TextView btnAddCategory;
        public TextView txtUserName;
        public ChipGroup chipGroupGoalFilters;

        public HeaderViewHolder(@NonNull View item_view) {
            super(item_view);
            lblActiveGoals = item_view.findViewById(R.id.lblActiveGoals);
            btnAddCategory = item_view.findViewById(R.id.btnAddCategory);
            txtUserName = item_view.findViewById(R.id.txtUserName);
            chipGroupGoalFilters = item_view.findViewById(R.id.chipGroupGoalFilters);
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(@NonNull View item_view) {
            super(item_view);
        }
    }
}
