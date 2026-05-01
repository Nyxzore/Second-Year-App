package com.example.gon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddEditGoal extends AppCompatActivity {

    private String userUuid;
    final String hosted_server = "https://wmc.ms.wits.ac.za/students/sgroup2689/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_goal);

        userUuid = getIntent().getStringExtra("USER_UUID");
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

        //sending post request to server to add goal into goals database
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("uuid", userUuid != null ? userUuid : "")
                    .add("description", description)
                    .add("title", title)
                    .add("due_date", due_date)
                    .add("mode", edit_mode ? "edit" : "add") //true if edit mode false if add mode
                    .add("goal_id", goal_id)
                    .build();

            Request request = new Request.Builder()
                    .url(hosted_server + "mutate_goal.php")
                    .post(formBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                final String responseData = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(responseData);
                        String status = json.getString("status");
                        String message = json.getString("message");

                        if (status.equals("success")) {
                            Toast.makeText(AddEditGoal.this, message, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(AddEditGoal.this, GoalList.class);
                            intent.putExtra("USER_UUID", userUuid);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(AddEditGoal.this, "Server: " + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        // 3. If it's not valid JSON, show the raw error (helps find PHP bugs)
                        Log.e("GON_DEBUG", "JSON Error: " + e.getMessage());
                        Toast.makeText(AddEditGoal.this, "Response Error: " + responseData, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Network Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                e.printStackTrace(); //no internet or server down
            }
        }).start();
    }
}