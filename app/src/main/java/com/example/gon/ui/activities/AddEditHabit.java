package com.example.gon.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.ChipGroup;

import com.example.gon.R;
import com.example.gon.Entities.Category;
import com.example.gon.ui.helpers.CategoryUiHelper;
import com.example.gon.utils.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AddEditHabit extends AppCompatActivity {

    private final ArrayList<Category> user_categories = new ArrayList<>();
    private final Set<String> selected_category_ids = new HashSet<>();
    private ChipGroup chip_group_habit_categories;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.activity_add_edit_habit);

        chip_group_habit_categories = findViewById(R.id.chipGroupHabitCategories);
        selected_category_ids.addAll(CategoryUiHelper.ids_from_comma_separated(
                getIntent().getStringExtra("category_ids")));

        boolean edit_mode = getIntent().getBooleanExtra("EDIT_MODE", false);
        Log.d("GON_DEBUG : HABIT", "Started AddEditHabit. Edit mode: " + edit_mode);

        TextView header = findViewById(R.id.textViewAddHabitTitle);
        EditText edt_title = findViewById(R.id.edtHabitTitle);
        EditText edt_description = findViewById(R.id.edtHabitDescription);
        Button btn_save = findViewById(R.id.btnSaveHabit);

        if (edit_mode) {
            header.setText("Edit Habit");
            btn_save.setText("Finish edit");
            edt_title.setText(getIntent().getStringExtra("name"));
            edt_description.setText(getIntent().getStringExtra("description"));
        }

        btn_save.setOnClickListener(v -> {
            Log.d("GON_DEBUG : HABIT", "Save button clicked");
            String title = edt_title.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Habit needs a title", Toast.LENGTH_SHORT).show();
                return;
            }
            btn_save.setEnabled(false);
            btn_save.setText("...");
            post_habit(edit_mode);
        });

        findViewById(R.id.btnAddCategory).setOnClickListener(v -> {
            Log.d("GON_DEBUG : HABIT", "Add category dialog requested");
            CategoryUiHelper.show_add_category_dialog(this, new_category -> {
                Log.d("GON_DEBUG : HABIT", "New category added: " + new_category.get_name());
                user_categories.add(new_category);
                selected_category_ids.add(new_category.get_id());
                CategoryUiHelper.bind_selectable_chips(this, chip_group_habit_categories, user_categories, selected_category_ids);
            });
        });

        load_categories();
    }

    private void load_categories() {
        Log.d("GON_DEBUG : HABIT", "Loading user categories...");
        Map<String, String> params = new HashMap<>();
        params.put("uuid", PreferenceManager.get_uuid(this));

        PreferenceManager.post("get_categories.php", params, response_data -> runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(response_data);
                if (!"success".equals(json.optString("status"))) {
                    Log.d("GON_DEBUG : HABIT", "Failed to load categories: " + json.optString("message"));
                    return;
                }
                user_categories.clear();
                user_categories.addAll(Category.list_from_json_array(json.getJSONArray("categories")));
                Log.d("GON_DEBUG : HABIT", "Categories loaded: " + user_categories.size());
                CategoryUiHelper.bind_selectable_chips(this, chip_group_habit_categories, user_categories, selected_category_ids);
            } catch (JSONException e) {
                Log.e("GON_DEBUG", "load_categories", e);
            }
        }));
    }

    private void post_habit(boolean edit_mode) {
        EditText edt_title = findViewById(R.id.edtHabitTitle);
        EditText edt_description = findViewById(R.id.edtHabitDescription);
        String name = edt_title.getText().toString().trim();

        Log.d("GON_DEBUG : HABIT", "Posting habit: " + name + ". Mode: " + (edit_mode ? "edit" : "add"));

        Map<String, String> params = new HashMap<>();
        params.put("uuid", PreferenceManager.get_uuid(this) != null ? PreferenceManager.get_uuid(this) : "");
        params.put("name", name);
        params.put("description", edt_description.getText().toString().trim());
        params.put("mode", edit_mode ? "edit" : "add");
        params.put("habit_id", getIntent().getStringExtra("habit_id") != null ? getIntent().getStringExtra("habit_id") : "-1");
        params.put("category_ids", CategoryUiHelper.comma_separated_ids(selected_category_ids));

        PreferenceManager.post("mutate_habit.php", params, response_data -> runOnUiThread(() -> {
            Button btn_save = findViewById(R.id.btnSaveHabit);
            btn_save.setEnabled(true);
            btn_save.setText(edit_mode ? "Finish edit" : "Save Habit");
            try {
                JSONObject json = new JSONObject(response_data);
                String status = json.getString("status");
                String message = json.optString("message", "");
                Log.d("GON_DEBUG : HABIT", "Habit post response: " + status + ". Message: " + message);

                if ("success".equals(status)) {
                    Toast.makeText(AddEditHabit.this, message.isEmpty() ? "Saved" : message, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddEditHabit.this, "Server: " + message, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Log.e("GON_DEBUG", "post_habit JSON", e);
                Toast.makeText(AddEditHabit.this, "Response: " + response_data, Toast.LENGTH_LONG).show();
            }
        }));
    }
}
