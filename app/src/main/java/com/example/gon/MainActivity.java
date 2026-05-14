package com.example.gon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {
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
        String savedUsername = PreferenceManager.getUsername(this);

        if (uuid != null && hash != null && savedUsername != null) {
            chkRememberMe.setChecked(true);
            findViewById(R.id.imageViewLogo).setVisibility(View.INVISIBLE);
            findViewById(R.id.textSubtitle).setVisibility(View.INVISIBLE);
            findViewById(R.id.loginCard).setVisibility(View.INVISIBLE);
            findViewById(R.id.autoLoading).setVisibility(View.VISIBLE);
            Log.d("GON_DEBUG : AUTH", "Starting automatic login for: " + savedUsername);
            authenticateUser("login", true);
        }
    }

    public void handleLogin(View v) {
        Log.d("GON_DEBUG : AUTH", "Login button clicked");
        authenticateUser("login", false);
    }

    public void handleCreateAccount(View v) {
        Log.d("GON_DEBUG : AUTH", "Create Account button clicked");
        Intent intent = new Intent(this, CreateAccount.class);
        startActivity(intent);
    }

    private void authenticateUser(String mode, boolean automatic_login) {
        TextView statusText = findViewById(R.id.textViewDebug);
        final String username;
        final String currentHash;

        if (automatic_login) {
            username = PreferenceManager.getUsername(this);
            currentHash = hash;
            if (username == null) {
                // Should not happen with new check in onCreate, but for safety:
                findViewById(R.id.imageViewLogo).setVisibility(View.VISIBLE);
                findViewById(R.id.textSubtitle).setVisibility(View.VISIBLE);
                findViewById(R.id.loginCard).setVisibility(View.VISIBLE);
                findViewById(R.id.autoLoading).setVisibility(View.GONE);
                return;
            }
        } else {
            username = username_edit.getText().toString().trim();
            String password = password_edit.getText().toString().trim();

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

        Log.d("GON_DEBUG : AUTH", "Sending request for mode: " + mode);
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("hash", currentHash);
        params.put("mode", mode);

        PreferenceManager.post("login.php", params, responseData -> {
            Log.d("GON_DEBUG : AUTH", "Response received for user: " + username);
            handleAuthResponse(responseData, statusText, username, currentHash);
        });
    }

    private void handleAuthResponse(String responseData, TextView statusText, String username, String currentHash) {
        try {
            org.json.JSONObject json = new org.json.JSONObject(responseData);
            String status = json.getString("status");
            String message = json.getString("message");

            if (status.equals("success")) {
                uuid = json.getString("uuid");

                PreferenceManager.saveUUID(this, uuid);
                PreferenceManager.saveUsername(this, username);

                if (chkRememberMe != null && chkRememberMe.isChecked()) {
                    PreferenceManager.saveHash(this, currentHash);
                } else {
                    // If not checked, clear any previously saved hash
                    PreferenceManager.saveHash(this, null);
                }

                Intent intent = new Intent(MainActivity.this, GoalList.class);
                startActivity(intent);
                finish();
            }
        else {
                android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show();
                statusText.setText(message);
                
                // Reset UI if automatic login failed
                findViewById(R.id.imageViewLogo).setVisibility(View.VISIBLE);
                findViewById(R.id.textSubtitle).setVisibility(View.VISIBLE);
                findViewById(R.id.loginCard).setVisibility(View.VISIBLE);
                findViewById(R.id.autoLoading).setVisibility(View.GONE);

                PreferenceManager.saveUUID(this, null);
                PreferenceManager.saveHash(this, null);
            }
        } catch (JSONException e) {
            android.widget.Toast.makeText(this, "Server Error: Invalid Response", android.widget.Toast.LENGTH_LONG).show();
            statusText.setText("Server Error: Invalid Response");
            Log.e("GON_DEBUG : AUTH", responseData);
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
