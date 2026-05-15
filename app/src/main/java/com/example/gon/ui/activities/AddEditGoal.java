package com.example.gon.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CalendarView;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AddEditGoal extends AppCompatActivity {

    private final ArrayList<Category> user_categories = new ArrayList<>();
    private final Set<String> selected_category_ids = new HashSet<>();
    private ChipGroup chip_group_goal_categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_goal);

        chip_group_goal_categories = findViewById(R.id.chipGroupGoalCategories);
        selected_category_ids.addAll(CategoryUiHelper.ids_from_comma_separated(
                getIntent().getStringExtra("category_ids")));

        boolean edit_mode = getIntent().getBooleanExtra("EDIT_MODE", false);
        Log.d("GON_DEBUG : GOAL", "Started AddEditGoal. Edit mode: " + edit_mode);

        TextView lbl_date = findViewById(R.id.lblDate);
        Button btn_add = findViewById(R.id.btnAdd);
        CalendarView calendar_view = findViewById(R.id.calendarView);
        EditText edt_description = findViewById(R.id.edtGoalDescription);
        EditText edt_title = findViewById(R.id.edtGoalTitle);

        if (edit_mode) {
            btn_add.setText("Finish edit");
            lbl_date.setText(getIntent().getStringExtra("due_date"));
            edt_title.setText(getIntent().getStringExtra("title"));
            edt_description.setText(getIntent().getStringExtra("description"));

            try {
                String date_str = getIntent().getStringExtra("due_date");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(date_str));
                calendar_view.setDate(cal.getTimeInMillis());
            } catch (Exception e) {
                Log.e("GON_DEBUG", "Date parse error: " + e.getMessage());
            }
        } else {
            calendar_view.setDate(System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            lbl_date.setText(sdf.format(System.currentTimeMillis()));
        }

        calendar_view.setOnDateChangeListener((view, year, month, day_of_month) -> {
            String selected_date = String.format("%d-%02d-%02d", year, (month + 1), day_of_month);
            lbl_date.setText(selected_date);
            Log.d("GON_DEBUG : GOAL", "Date changed to: " + selected_date);
            if (!edit_mode) {
                btn_add.setText("Add");
            }
        });

        btn_add.setOnClickListener(v -> {
            Log.d("GON_DEBUG : GOAL", "Add/Edit button clicked");
            if (edt_title.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "A goal must have a title", Toast.LENGTH_SHORT).show();
                return;
            }

            btn_add.setEnabled(false);
            btn_add.setText("...");
            post_goal(edit_mode);
        });

        findViewById(R.id.btnAddCategoryGoal).setOnClickListener(v -> {
            Log.d("GON_DEBUG : GOAL", "Add category dialog requested");
            CategoryUiHelper.show_add_category_dialog(this, new_category -> {
                Log.d("GON_DEBUG : GOAL", "New category added: " + new_category.get_name());
                user_categories.add(new_category);
                selected_category_ids.add(new_category.get_id());
                CategoryUiHelper.bind_selectable_chips(this, chip_group_goal_categories, user_categories, selected_category_ids);
            });
        });

        load_categories();
    }

    private void load_categories() {
        Log.d("GON_DEBUG : GOAL", "Loading user categories...");
        Map<String, String> params = new HashMap<>();
        params.put("uuid", PreferenceManager.get_uuid(this));

        PreferenceManager.post("get_categories.php", params, response_data -> runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(response_data);
                if (!"success".equals(json.optString("status"))) {
                    Log.d("GON_DEBUG : GOAL", "Failed to load categories: " + json.optString("message"));
                    return;
                }
                user_categories.clear();
                user_categories.addAll(Category.list_from_json_array(json.getJSONArray("categories")));
                Log.d("GON_DEBUG : GOAL", "Categories loaded: " + user_categories.size());
                CategoryUiHelper.bind_selectable_chips(this, chip_group_goal_categories, user_categories, selected_category_ids);
            } catch (JSONException e) {
                Log.e("GON_DEBUG", "load_categories", e);
            }
        }));
    }

    public void post_goal(Boolean edit_mode) {
        EditText edt_description = findViewById(R.id.edtGoalDescription);
        EditText edt_title = findViewById(R.id.edtGoalTitle);
        TextView lbl_date = findViewById(R.id.lblDate);
        final String description = edt_description.getText().toString();
        final String title = edt_title.getText().toString();
        final String due_date = lbl_date.getText().toString();
        final String goal_id = getIntent().getStringExtra("goal_id") != null ? getIntent().getStringExtra("goal_id") : "-1";

        Log.d("GON_DEBUG : GOAL", "Posting goal: " + title + ". Mode: " + (edit_mode ? "edit" : "add"));

        Map<String, String> params = new HashMap<>();
        params.put("uuid", PreferenceManager.get_uuid(this) != null ? PreferenceManager.get_uuid(this) : "");
        params.put("description", description);
        params.put("title", title);
        params.put("due_date", due_date);
        params.put("mode", edit_mode ? "edit" : "add");
        params.put("goal_id", goal_id);
        params.put("category_ids", CategoryUiHelper.comma_separated_ids(selected_category_ids));

        PreferenceManager.post("mutate_goal.php", params, response_data -> runOnUiThread(() -> {
            Button btn_add = findViewById(R.id.btnAdd);
            btn_add.setEnabled(true);
            btn_add.setText(edit_mode ? "Finish edit" : "CREATE GOAL");
            try {
                JSONObject json = new JSONObject(response_data);
                String status = json.getString("status");
                String message = json.getString("message");
                Log.d("GON_DEBUG : GOAL", "Goal post response: " + status + ". Message: " + message);

                if (status.equals("success")) {
                    Toast.makeText(AddEditGoal.this, message, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(AddEditGoal.this, "Server: " + message, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Log.e("GON_DEBUG : GOAL", "JSON Error: " + e.getMessage());
                Toast.makeText(AddEditGoal.this, "Response Error: " + response_data, Toast.LENGTH_LONG).show();
            }
        }));
    }
}
