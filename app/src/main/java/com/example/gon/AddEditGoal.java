package com.example.gon;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.ChipGroup;

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

    private final ArrayList<Category> userCategories = new ArrayList<>();
    private final Set<String> selectedCategoryIds = new HashSet<>();
    private ChipGroup chipGroupGoalCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_goal);

        chipGroupGoalCategories = findViewById(R.id.chipGroupGoalCategories);
        selectedCategoryIds.addAll(CategoryUiHelper.idsFromCommaSeparated(
                getIntent().getStringExtra("category_ids")));

        boolean edit_mode = getIntent().getBooleanExtra("EDIT_MODE", false);

        TextView lblDate = findViewById(R.id.lblDate);
        Button btnAdd = findViewById(R.id.btnAdd);
        CalendarView calendarView = findViewById(R.id.calendarView);
        EditText edtDescription = findViewById(R.id.edtGoalDescription);
        EditText edtTitle = findViewById(R.id.edtGoalTitle);

        if (edit_mode) {
            btnAdd.setText("Finish edit");
            lblDate.setText(getIntent().getStringExtra("due_date"));
            edtTitle.setText(getIntent().getStringExtra("title"));
            edtDescription.setText(getIntent().getStringExtra("description"));

            try {
                String dateStr = getIntent().getStringExtra("due_date");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(dateStr));
                calendarView.setDate(cal.getTimeInMillis());
            } catch (Exception e) {
                Log.e("GON_DEBUG", "Date parse error: " + e.getMessage());
            }
        } else {
            calendarView.setDate(System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            lblDate.setText(sdf.format(System.currentTimeMillis()));
        }

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = String.format("%d-%02d-%02d", year, (month + 1), dayOfMonth);
            lblDate.setText(selectedDate);
            if (!edit_mode) {
                btnAdd.setText("Add");
            }
        });

        btnAdd.setOnClickListener(v -> {
            if (edtTitle.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "A goal must have a title", Toast.LENGTH_SHORT).show();
                return;
            }

            btnAdd.setEnabled(false);
            btnAdd.setText("...");
            post_goal(edit_mode);
        });

        findViewById(R.id.btnAddCategoryGoal).setOnClickListener(v -> {
            CategoryUiHelper.showAddCategoryDialog(this, newCategory -> {
                userCategories.add(newCategory);
                selectedCategoryIds.add(newCategory.getId());
                CategoryUiHelper.bindSelectableChips(this, chipGroupGoalCategories, userCategories, selectedCategoryIds);
            });
        });

        loadCategories();
    }

    private void loadCategories() {
        Map<String, String> params = new HashMap<>();
        params.put("uuid", PreferenceManager.getUUID(this));

        PreferenceManager.post("get_categories.php", params, responseData -> runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(responseData);
                if (!"success".equals(json.optString("status"))) {
                    return;
                }
                userCategories.clear();
                userCategories.addAll(Category.listFromJsonArray(json.getJSONArray("categories")));
                CategoryUiHelper.bindSelectableChips(this, chipGroupGoalCategories, userCategories, selectedCategoryIds);
            } catch (JSONException e) {
                Log.e("GON_DEBUG", "loadCategories", e);
            }
        }));
    }

    public void post_goal(Boolean edit_mode) {
        EditText edtDescription = findViewById(R.id.edtGoalDescription);
        EditText edtTitle = findViewById(R.id.edtGoalTitle);
        TextView lblDate = findViewById(R.id.lblDate);
        final String description = edtDescription.getText().toString();
        final String title = edtTitle.getText().toString();
        final String due_date = lblDate.getText().toString();
        final String goal_id = getIntent().getStringExtra("goal_id") != null ? getIntent().getStringExtra("goal_id") : "-1";

        Map<String, String> params = new HashMap<>();
        params.put("uuid", PreferenceManager.getUUID(this) != null ? PreferenceManager.getUUID(this) : "");
        params.put("description", description);
        params.put("title", title);
        params.put("due_date", due_date);
        params.put("mode", edit_mode ? "edit" : "add");
        params.put("goal_id", goal_id);
        params.put("category_ids", CategoryUiHelper.commaSeparatedIds(selectedCategoryIds));

        PreferenceManager.post("mutate_goal.php", params, responseData -> runOnUiThread(() -> {
            Button btnAdd = findViewById(R.id.btnAdd);
            btnAdd.setEnabled(true);
            btnAdd.setText(edit_mode ? "Finish edit" : "CREATE GOAL");
            try {
                JSONObject json = new JSONObject(responseData);
                String status = json.getString("status");
                String message = json.getString("message");

                if (status.equals("success")) {
                    Toast.makeText(AddEditGoal.this, message, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(AddEditGoal.this, "Server: " + message, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Log.e("GON_DEBUG", "JSON Error: " + e.getMessage());
                Toast.makeText(AddEditGoal.this, "Response Error: " + responseData, Toast.LENGTH_LONG).show();
            }
        }));
    }
}
