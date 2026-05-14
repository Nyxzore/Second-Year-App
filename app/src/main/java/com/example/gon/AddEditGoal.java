package com.example.gon;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddEditGoal extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_goal);

        //Category Selection
        LinearLayout fitness = findViewById(R.id.layoutFitness);
        LinearLayout health = findViewById(R.id.layoutHealth);
        LinearLayout learning = findViewById(R.id.layoutLearning);
        LinearLayout other = findViewById(R.id.layoutOther);

        LinearLayout[] categories = {fitness, health, learning, other};

        View.OnClickListener categoryClickListener = v -> {

            //Reset all categories
            for (LinearLayout category : categories) {
                category.setBackgroundResource(R.drawable.category_unselected);
            }

            //Highlight selected category
            v.setBackgroundResource(R.drawable.category_selected);
        };

        fitness.setOnClickListener(categoryClickListener);
        health.setOnClickListener(categoryClickListener);
        learning.setOnClickListener(categoryClickListener);
        other.setOnClickListener(categoryClickListener);

        boolean edit_mode = getIntent().getBooleanExtra("EDIT_MODE", false);

        TextView lblDate = (TextView) findViewById(R.id.lblDate);
        Button btnAdd = (Button) findViewById(R.id.btnAdd);
        CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView);
        EditText edtDescription = (EditText) findViewById(R.id.edtGoalDescription);
        EditText edtTitle = (EditText) findViewById(R.id.edtGoalTitle);

        if (edit_mode){
            btnAdd.setText("Finish edit");
            lblDate.setText(getIntent().getStringExtra("due_date"));
            edtTitle.setText(getIntent().getStringExtra("title"));
            edtDescription.setText(getIntent().getStringExtra("description"));

            // Attempt to set the calendar to the existing due date
            try {
                String dateStr = getIntent().getStringExtra("due_date");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(dateStr));
                calendarView.setDate(cal.getTimeInMillis());
            } catch (Exception e) {
                Log.e("GON_DEBUG", "Date parse error: " + e.getMessage());
            }
        }
        else {
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
            if (edtTitle.getText().toString().trim().isEmpty()){
                Toast.makeText(this, "A goal must have a title", Toast.LENGTH_SHORT).show();
                return;
            }

            btnAdd.setEnabled(false);
            btnAdd.setText("...");
            post_goal(edit_mode);
        });
    }

    public void post_goal(Boolean edit_mode){
        //retrieving data from UI
        EditText edtDescription = (EditText) findViewById(R.id.edtGoalDescription);
        EditText edtTitle = (EditText) findViewById(R.id.edtGoalTitle);
        TextView lblDate = (TextView) findViewById(R.id.lblDate);
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

            PreferenceManager.post("mutate_goal.php", params, responseData -> {
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
        });
    }
}