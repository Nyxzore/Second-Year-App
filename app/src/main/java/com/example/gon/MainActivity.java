package com.example.gon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    final String hosted_server = "https://wmc.ms.wits.ac.za/students/sgroup2689/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void handleLogin(View v) {
        authenticateUser("login");
    }

    public void handleCreateAccount(View v) {
        authenticateUser("create_account");
    }

    private void authenticateUser(String mode) {
        EditText username_edit = findViewById(R.id.edtUsername);
        EditText password_edit = findViewById(R.id.edtPassword);
        TextView statusText = findViewById(R.id.textViewDebug);

        String username = username_edit.getText().toString();
        String password = password_edit.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            statusText.setText("Please enter both username and password");
            return;
        }

        new Thread(() -> {
            try {
                String hashed_password = sha256(password);
                OkHttpClient client = new OkHttpClient();

                RequestBody formBody = new FormBody.Builder()
                        .add("username", username)
                        .add("hash", hashed_password)
                        .add("mode", mode)
                        .build();

                Request request = new Request.Builder()
                        .url(hosted_server + "login.php")
                        .post(formBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        runOnUiThread(() -> handleAuthResponse(responseData, statusText));
                    }
                }
            } catch (NoSuchAlgorithmException e) {
                runOnUiThread(() -> statusText.setText("Hashing Error"));
            } catch (IOException e) {
                runOnUiThread(() -> statusText.setText("Connection failed. Check internet."));
            }
        }).start();
    }

    private void handleAuthResponse(String responseData, TextView statusText) {
        try {
            org.json.JSONObject json = new org.json.JSONObject(responseData);
            String status = json.getString("status");
            String message = json.getString("message");

            if (status.equals("success")) {
                String uuid = json.getString("uuid");
                Intent intent = new Intent(MainActivity.this, GoalList.class);
                intent.putExtra("USER_UUID", uuid);
                startActivity(intent);
                finish(); // Close login screen
            }
            statusText.setText(message);
        } catch (JSONException e) {
            statusText.setText("Server Error: Invalid Response");
            Log.e("AUTH_ERROR", responseData);
        }
    }

    public String sha256(String raw_data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(raw_data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
        for (byte b : encodedhash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
