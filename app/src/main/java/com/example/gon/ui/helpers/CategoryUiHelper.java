package com.example.gon.ui.helpers;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import com.example.gon.R;
import com.example.gon.Entities.Category;
import com.example.gon.utils.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CategoryUiHelper {

    public interface FilterListener {
        void on_filter_changed(String category_id_or_null);
    }

    public interface CategoryAddedListener {
        void on_category_added(Category new_category);
    }

    private CategoryUiHelper() {
    }

    public static void show_add_category_dialog(Context context, CategoryAddedListener listener) {
        Log.d("GON_DEBUG : CATEGORY_UI", "Showing add category dialog");
        EditText input = new EditText(context);
        input.setHint("Category name");
        int padding = (int) (24 * context.getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(context)
                .setTitle("New Category")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    Log.d("GON_DEBUG : CATEGORY_UI", "Dialog Add clicked. Name: " + name);
                    if (!name.isEmpty()) {
                        post_category(context, name, listener);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> Log.d("GON_DEBUG : CATEGORY_UI", "Dialog Cancel clicked"))
                .show();
    }

    private static void post_category(Context context, String name, CategoryAddedListener listener) {
        Log.d("GON_DEBUG : CATEGORY_UI", "Posting new category: " + name);
        Map<String, String> params = new HashMap<>();
        params.put("uuid", PreferenceManager.get_uuid(context));
        params.put("category_name", name);

        PreferenceManager.post("add_category.php", params, response_data -> {
            Log.d("GON_DEBUG : CATEGORY_UI", "Category post response: " + response_data);
            try {
                JSONObject json = new JSONObject(response_data);
                if ("success".equals(json.optString("status"))) {
                    Category new_cat = Category.from_json(json.getJSONObject("category"));
                    Log.d("GON_DEBUG : CATEGORY_UI", "Category added successfully: " + new_cat.get_name());
                    if (context instanceof AppCompatActivity) {
                        ((AppCompatActivity) context).runOnUiThread(() -> listener.on_category_added(new_cat));
                    }
                } else {
                    String msg = json.optString("message", "Error adding category");
                    Log.d("GON_DEBUG : CATEGORY_UI", "Category add failed: " + msg);
                    if (context instanceof AppCompatActivity) {
                        ((AppCompatActivity) context).runOnUiThread(() ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (JSONException e) {
                Log.e("GON_DEBUG : CATEGORY_UI", "post_category error", e);
            }
        });
    }

    public static void bind_display_chips(Context context, ChipGroup chip_group, List<Category> categories) {
        if (chip_group == null) {
            return;
        }
        chip_group.removeAllViews();
        if (categories == null || categories.isEmpty()) {
            chip_group.setVisibility(View.GONE);
            return;
        }
        chip_group.setVisibility(View.VISIBLE);
        float density = context.getResources().getDisplayMetrics().density;
        int margin = (int) (6 * density);
        int pad_h = (int) (10 * density);
        int pad_v = (int) (4 * density);
        int forest_green = ContextCompat.getColor(context, R.color.forest_green);
        for (Category category : categories) {
            TextView pill = new TextView(context);
            pill.setText(category.get_name());
            pill.setTextColor(forest_green);
            pill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            pill.setBackgroundResource(R.drawable.category_selected);
            pill.setPadding(pad_h, pad_v, pad_h, pad_v);
            pill.setClickable(false);
            pill.setFocusable(false);
            ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.setMarginEnd(margin);
            lp.bottomMargin = margin;
            pill.setLayoutParams(lp);
            chip_group.addView(pill);
        }
        chip_group.requestLayout();
    }

    public static void bind_filter_chips(Context context, ChipGroup chip_group, List<Category> categories,
                                       String selected_category_id, FilterListener listener) {
        chip_group.setSingleSelection(true);
        chip_group.setSelectionRequired(false);
        chip_group.removeAllViews();
        
        chip_group.addView(make_filter_chip(context, chip_group, "All", selected_category_id == null, null,
                () -> listener.on_filter_changed(null)));

        for (Category category : categories) {
            boolean selected = category.get_id().equals(selected_category_id);
            chip_group.addView(make_filter_chip(context, chip_group, category.get_name(), selected, category.get_id(),
                    () -> listener.on_filter_changed(category.get_id())));
        }
    }

    public static void bind_selectable_chips(Context context, ChipGroup chip_group, List<Category> categories,
                                           Set<String> selected_ids) {
        chip_group.removeAllViews();
        for (Category category : categories) {
            Chip chip = new Chip(context);
            chip.setText(category.get_name());
            chip.setCheckable(true);
            chip.setChecked(selected_ids.contains(category.get_id()));
            chip.setTag(category.get_id());
            chip.setOnCheckedChangeListener((button_view, is_checked) -> {
                String id = (String) button_view.getTag();
                Log.d("GON_DEBUG : CATEGORY_UI", "Chip " + id + " toggled: " + is_checked);
                if (is_checked) {
                    selected_ids.add(id);
                } else {
                    selected_ids.remove(id);
                }
            });
            chip_group.addView(chip);
        }
    }

    public static Set<String> ids_from_comma_separated(String raw) {
        Set<String> ids = new HashSet<>();
        if (raw == null || raw.isEmpty()) {
            return ids;
        }
        for (String part : raw.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                ids.add(trimmed);
            }
        }
        return ids;
    }

    public static String comma_separated_ids(Set<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String id : ids) {
            if (!first) {
                sb.append(",");
            }
            sb.append(id);
            first = false;
        }
        return sb.toString();
    }

    private static Chip make_filter_chip(Context context, ChipGroup group, String label, boolean selected,
                                       String id, Runnable on_click) {
        Chip chip = new Chip(context);
        chip.setText(label);
        chip.setCheckable(true);
        chip.setChecked(selected);
        chip.setTag(id);
        chip.setOnClickListener(v -> {
            Log.d("GON_DEBUG : CATEGORY_UI", "Filter chip clicked: " + label + " (id=" + id + ")");
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof Chip) {
                    ((Chip) child).setChecked(child == v);
                }
            }
            on_click.run();
        });
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int margin = (int) (6 * context.getResources().getDisplayMetrics().density);
        lp.setMarginEnd(margin);
        chip.setLayoutParams(lp);
        return chip;
    }
}
