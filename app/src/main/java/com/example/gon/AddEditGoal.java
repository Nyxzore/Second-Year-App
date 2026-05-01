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

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.nio.channels.ScatteringByteChannel;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddGoal extends AppCompatActivity {

    private String userUuid;
    final String hosted_server = "https://wmc.ms.wits.ac.za/students/sgroup2689/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_goal);

        userUuid = getIntent().getStringExtra("USER_UUID");

        TextView lblDate = (TextView) findViewById(R.id.lblDate);
        Button btnAdd = (Button) findViewById(R.id.btnAdd);
        CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = String.format("%d-%02d-%02d", year, (month + 1), dayOfMonth);//stupid ah 0 indexing on month
            lblDate.setText(selectedDate);
        });

        btnAdd.setOnClickListener(v -> {
            btnAdd.setEnabled(false);
            btnAdd.setText("Posting...");
            post_goal();
        });
    }

    public void post_goal(){
        //retrieving data from UI
        EditText edtDescription = (EditText) findViewById(R.id.edtGoalDescription);
        EditText edtTitle = (EditText) findViewById(R.id.edtGoalTitle);
        TextView lblDate = (TextView) findViewById(R.id.lblDate);
        final String description = edtDescription.getText().toString();
        final String title = edtTitle.getText().toString();
        final String due_date = lblDate.getText().toString();

        //sending post request to server to add goal into goals database
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("uuid", userUuid)
                    .add("description", description)
                    .add("title", title)
                    .add("due_date", due_date)
                    .build();

            Request request = new Request.Builder()
                    .url(hosted_server + "add_goal.php")
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
                            Toast.makeText(AddGoal.this, message, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(AddGoal.this, GoalList.class);
                            intent.putExtra("USER_UUID", userUuid);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(AddGoal.this, "Server: " + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        // 3. If it's not valid JSON, show the raw error (helps find PHP bugs)
                        Log.e("GON_DEBUG", "JSON Error: " + e.getMessage());
                        Toast.makeText(AddGoal.this, "Response Error: " + responseData, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Network Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                e.printStackTrace(); //no internet or server down
            }
        }).start();
    }
}