package com.example.gon;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CategoryUiHelper {

    public interface FilterListener {
        void onFilterChanged(String categoryIdOrNull);
    }

    public interface CategoryAddedListener {
        void onCategoryAdded(Category newCategory);
    }

    private CategoryUiHelper() {
    }

    public static void showAddCategoryDialog(Context context, CategoryAddedListener listener) {
        EditText input = new EditText(context);
        input.setHint("Category name");
        int padding = (int) (24 * context.getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(context)
                .setTitle("New Category")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        postCategory(context, name, listener);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static void postCategory(Context context, String name, CategoryAddedListener listener) {
        Map<String, String> params = new HashMap<>();
        params.put("uuid", PreferenceManager.getUUID(context));
        params.put("category_name", name);

        PreferenceManager.post("add_category.php", params, responseData -> {
            try {
                JSONObject json = new JSONObject(responseData);
                if ("success".equals(json.optString("status"))) {
                    Category newCat = Category.fromJson(json.getJSONObject("category"));
                    if (context instanceof AppCompatActivity) {
                        ((AppCompatActivity) context).runOnUiThread(() -> listener.onCategoryAdded(newCat));
                    }
                } else {
                    String msg = json.optString("message", "Error adding category");
                    if (context instanceof AppCompatActivity) {
                        ((AppCompatActivity) context).runOnUiThread(() ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    public static void bindFilterChips(Context context, ChipGroup chipGroup, List<Category> categories,
                                       String selectedCategoryId, FilterListener listener) {
        if (chipGroup.getChildCount() > 0 && categories.size() + 1 == chipGroup.getChildCount()) {
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                View child = chipGroup.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;
                    String id = (String) chip.getTag();
                    chip.setChecked((id == null && selectedCategoryId == null) || (id != null && id.equals(selectedCategoryId)));
                }
            }
            return;
        }

        chipGroup.setSingleSelection(true);
        chipGroup.setSelectionRequired(false);
        chipGroup.removeAllViews();

        chipGroup.addView(makeFilterChip(context, chipGroup, "All", selectedCategoryId == null, null,
                () -> listener.onFilterChanged(null)));

        for (Category category : categories) {
            boolean selected = category.getId().equals(selectedCategoryId);
            chipGroup.addView(makeFilterChip(context, chipGroup, category.getName(), selected, category.getId(),
                    () -> listener.onFilterChanged(category.getId())));
        }
    }

    public static void bindSelectableChips(Context context, ChipGroup chipGroup, List<Category> categories,
                                           Set<String> selectedIds) {
        chipGroup.removeAllViews();
        for (Category category : categories) {
            Chip chip = new Chip(context);
            chip.setText(category.getName());
            chip.setCheckable(true);
            chip.setChecked(selectedIds.contains(category.getId()));
            chip.setTag(category.getId());
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String id = (String) buttonView.getTag();
                if (isChecked) {
                    selectedIds.add(id);
                } else {
                    selectedIds.remove(id);
                }
            });
            chipGroup.addView(chip);
        }
    }

    public static Set<String> idsFromCommaSeparated(String raw) {
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

    public static String commaSeparatedIds(Set<String> ids) {
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

    private static Chip makeFilterChip(Context context, ChipGroup group, String label, boolean selected,
                                       String id, Runnable onClick) {
        Chip chip = new Chip(context);
        chip.setText(label);
        chip.setCheckable(true);
        chip.setChecked(selected);
        chip.setTag(id);
        chip.setOnClickListener(v -> {
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof Chip) {
                    ((Chip) child).setChecked(child == v);
                }
            }
            onClick.run();
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
