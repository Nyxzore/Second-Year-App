package com.example.gon;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    String uuid, hash;
    EditText username_edit, password_edit;
    CheckBox chkRememberMe;
    TextView textViewDebug;

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


        username_edit = findViewById(R.id.edtUsername);
        password_edit = findViewById(R.id.edtPassword);
        chkRememberMe = findViewById(R.id.chkRememberMe);
        textViewDebug = findViewById(R.id.textViewDebug);

        uuid = PreferenceManager.getUUID(this);
        hash = PreferenceManager.getHashString(this);

        if (uuid != null && hash != null) {
            findViewById(R.id.textViewTitle).setVisibility(View.INVISIBLE);
            findViewById(R.id.imageViewLogo).setVisibility(View.INVISIBLE);
            findViewById(R.id.textViewUsername).setVisibility(View.INVISIBLE);
            findViewById(R.id.edtUsername).setVisibility(View.INVISIBLE);
            findViewById(R.id.textViewPassword).setVisibility(View.INVISIBLE);
            findViewById(R.id.edtPassword).setVisibility(View.INVISIBLE);
            findViewById(R.id.btnLogIn).setVisibility(View.INVISIBLE);
            findViewById(R.id.btnCreateAccount).setVisibility(View.INVISIBLE);
            findViewById(R.id.chkRememberMe).setVisibility(View.INVISIBLE);
            findViewById(R.id.loading).setVisibility(View.VISIBLE);
            authenticateUser("login", true);
        }
    }

    public void handleLogin(View v) {
        authenticateUser("login", false);
    }

    public void handleCreateAccount(View v) {
        authenticateUser("create_account", false);
    }

    private void authenticateUser(String mode, boolean automatic_login) {
        TextView statusText = findViewById(R.id.textViewDebug);
        final String username;
        final String currentHash;

        if (automatic_login) {
            username = PreferenceManager.getUsername(this);
            currentHash = hash;
            if (username == null) return;
        } else {
            username = username_edit.getText().toString();
            String password = password_edit.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                statusText.setText("Please enter both username and password");
                return;
            }
            try {
                currentHash = sha256(password);
            } catch (NoSuchAlgorithmException e) {
                statusText.setText("Hashing Error");
                return;
            }
        }

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                RequestBody formBody = new FormBody.Builder()
                        .add("username", username)
                        .add("hash", currentHash)
                        .add("mode", mode)
                        .build();

                Request request = new Request.Builder()
                        .url(hosted_server + "login.php")
                        .post(formBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        runOnUiThread(() -> handleAuthResponse(responseData, statusText, username, currentHash));
                    } else {
                        runOnUiThread(() -> statusText.setText("Server returned error: " + response.code()));
                    }
                }
            } catch (IOException e) {
                runOnUiThread(() -> statusText.setText("Connection failed. Check internet."));
            }
        }).start();
    }

    private void handleAuthResponse(String responseData, TextView statusText, String username, String currentHash) {
        try {
            org.json.JSONObject json = new org.json.JSONObject(responseData);
            String status = json.getString("status");
            String message = json.getString("message");

            if (status.equals("success")) {
                uuid = json.getString("uuid");
                
                // Only save if it's a manual login and checkbox is checked, 
                // OR if it's already an automatic login (to keep it refreshed)
                CheckBox chkRemember = findViewById(R.id.chkRememberMe);
                if (chkRemember.isChecked()) {
                    PreferenceManager.saveUUID(this, uuid);
                    PreferenceManager.saveHash(this, currentHash);
                    PreferenceManager.saveUsername(this, username);
                }

                Intent intent = new Intent(MainActivity.this, GoalList.class);
                intent.putExtra("USER_UUID", uuid);
                startActivity(intent);
                finish();
            } else {
                statusText.setText(message);
                // If automatic login fails (e.g. password changed), clear preferences
                PreferenceManager.saveUUID(this, null);
                PreferenceManager.saveHash(this, null);
            }
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
