package com.example.gon.ui.adapters;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.chip.ChipGroup;

import com.example.gon.R;
import com.example.gon.models.Goal;
import com.example.gon.ui.helpers.CategoryUiHelper;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;

    private final List<Goal> goal_list;
    private HeaderViewHolder headerViewHolder;
    private HeaderBindListener headerBindListener;

    public interface HeaderBindListener {
        void onBindHeader(HeaderViewHolder holder);
    }

    public GoalAdapter(List<Goal> goal_list) {
        this.goal_list = goal_list;
    }

    public void setHeaderBindListener(HeaderBindListener listener) {
        this.headerBindListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        if (position == goal_list.size() + 1) return TYPE_FOOTER;
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_goal_list, parent, false);
            headerViewHolder = new HeaderViewHolder(view);
            return headerViewHolder;
        } else if (viewType == TYPE_FOOTER) {
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
            headerViewHolder = (HeaderViewHolder) holder;
            if (headerBindListener != null) {
                headerBindListener.onBindHeader((HeaderViewHolder) holder);
            }
        } else if (holder instanceof GoalViewHolder) {
            Goal currentGoal = goal_list.get(position - 1);
            GoalViewHolder gHolder = (GoalViewHolder) holder;
            gHolder.textViewGoalName.setText(currentGoal.getTitle());
            gHolder.textViewGoalDescription.setText(currentGoal.getDescription());
            gHolder.textViewGoalDate.setText(holder.itemView.getContext().getString(R.string.plan_to_do_by, currentGoal.getDueDate()));

            try {
                Date due_date = currentGoal.getDueDateAsDate();
                Date today = new Date();
                if (today.after(due_date)) {
                    gHolder.textViewStatus.setText(R.string.status_overdue);
                    gHolder.textViewStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.overdue_date));
                } else {
                    gHolder.textViewStatus.setText(R.string.status_active);
                    gHolder.textViewStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.ontrack_date));
                }
            } catch (ParseException e) {
                gHolder.textViewStatus.setText(R.string.status_active);
                gHolder.textViewStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.ontrack_date));
            }

            CategoryUiHelper.bindDisplayChips(holder.itemView.getContext(),
                    gHolder.chipGroupGoalCategories, currentGoal.getCategories());
        }
    }

    @Override
    public int getItemCount() {
        // Header + List + Footer
        return goal_list.size() + 2;
    }

    public HeaderViewHolder getHeaderViewHolder() {
        return headerViewHolder;
    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView textViewGoalName;
        TextView textViewGoalDescription;
        TextView textViewGoalDate;
        TextView textViewStatus;
        ChipGroup chipGroupGoalCategories;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGoalName = itemView.findViewById(R.id.textViewGoalName);
            textViewGoalDescription = itemView.findViewById(R.id.textViewGoalDescription);
            textViewGoalDate = itemView.findViewById(R.id.textViewGoalDate);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            chipGroupGoalCategories = itemView.findViewById(R.id.chipGroupGoalCategories);

            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(this.getBindingAdapterPosition(), 101, 0, "Edit");
            menu.add(this.getBindingAdapterPosition(), 102, 1, "Delete");
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView lblActiveGoals;
        public TextView btnAddCategory;
        public TextView txtUserName;
        public ChipGroup chipGroupGoalFilters;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            lblActiveGoals = itemView.findViewById(R.id.lblActiveGoals);
            btnAddCategory = itemView.findViewById(R.id.btnAddCategory);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            chipGroupGoalFilters = itemView.findViewById(R.id.chipGroupGoalFilters);
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}