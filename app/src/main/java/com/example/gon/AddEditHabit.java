package com.example.gon;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddEditHabit extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_habit);

        boolean editMode = getIntent().getBooleanExtra("EDIT_MODE", false);
        TextView header = findViewById(R.id.textViewAddHabitTitle);
        EditText edtTitle = findViewById(R.id.edtHabitTitle);
        EditText edtDescription = findViewById(R.id.edtHabitDescription);
        Button btnSave = findViewById(R.id.btnSaveHabit);

        if (editMode) {
            header.setText("Edit Habit");
            edtTitle.setText(getIntent().getStringExtra("name"));
            edtDescription.setText(getIntent().getStringExtra("description"));
        }

        btnSave.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Habit needs a title", Toast.LENGTH_SHORT).show();
                return;
            }
            btnSave.setEnabled(false);
            btnSave.setText("...");
            postHabit(editMode);
        });
    }

    private void postHabit(boolean editMode) {
        EditText edtTitle = findViewById(R.id.edtHabitTitle);
        EditText edtDescription = findViewById(R.id.edtHabitDescription);

        Map<String, String> params = new HashMap<>();
        params.put("uuid", PreferenceManager.getUUID(this) != null ? PreferenceManager.getUUID(this) : "");
        params.put("name", edtTitle.getText().toString().trim());
        params.put("description", edtDescription.getText().toString().trim());
        params.put("mode", editMode ? "edit" : "add");
        params.put("habit_id", getIntent().getStringExtra("habit_id") != null ? getIntent().getStringExtra("habit_id") : "-1");

        PreferenceManager.post("mutate_habit.php", params, responseData -> runOnUiThread(() -> {
            Button btnSave = findViewById(R.id.btnSaveHabit);
            btnSave.setEnabled(true);
            btnSave.setText("Save Habit");
            try {
                JSONObject json = new JSONObject(responseData);
                String status = json.getString("status");
                String message = json.optString("message", "");
                if ("success".equals(status)) {
                    Toast.makeText(AddEditHabit.this, message.isEmpty() ? "Saved" : message, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddEditHabit.this, "Server: " + message, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Log.e("GON_DEBUG", "postHabit JSON", e);
                Toast.makeText(AddEditHabit.this, "Response: " + responseData, Toast.LENGTH_LONG).show();
            }
        }));
    }
}