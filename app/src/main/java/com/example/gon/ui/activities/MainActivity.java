package com.example.gon.ui.activities;

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

import com.example.gon.R;
import com.example.gon.utils.PreferenceManager;

import org.json.JSONException;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {
    String uuid, hash;
    EditText username_edit, password_edit;
    CheckBox chk_remember_me;
    TextView text_view_debug;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets system_bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(system_bars.left, system_bars.top, system_bars.right, system_bars.bottom);
            return insets;
        });

        username_edit = findViewById(R.id.edtUsername);
        password_edit = findViewById(R.id.edtPassword);
        chk_remember_me = findViewById(R.id.chkRememberMe);
        text_view_debug = findViewById(R.id.textViewDebug);

        uuid = PreferenceManager.get_uuid(this);
        hash = PreferenceManager.get_hash_string(this);
        String saved_username = PreferenceManager.get_username(this);

        if (uuid != null && hash != null && saved_username != null) {
            chk_remember_me.setChecked(true);
            findViewById(R.id.viewCircle).setVisibility(View.INVISIBLE);
            findViewById(R.id.imageViewLogo).setVisibility(View.INVISIBLE);
            findViewById(R.id.textSubtitle).setVisibility(View.INVISIBLE);
            findViewById(R.id.loginCard).setVisibility(View.INVISIBLE);
            findViewById(R.id.autoLoading).setVisibility(View.VISIBLE);
            Log.d("GON_DEBUG : AUTH", "Starting automatic login for: " + saved_username);
            authenticate_user("login", true);
        }
    }

    public void handle_login(View v) {
        Log.d("GON_DEBUG : AUTH", "Login button clicked");
        authenticate_user("login", false);
    }

    public void handle_create_account(View v) {
        Log.d("GON_DEBUG : AUTH", "Create Account button clicked");
        Intent intent = new Intent(this, CreateAccount.class);
        startActivity(intent);
    }

    private void authenticate_user(String mode, boolean automatic_login) {
        TextView status_text = findViewById(R.id.textViewDebug);
        final String username;
        final String current_hash;

        if (automatic_login) {
            username = PreferenceManager.get_username(this);
            current_hash = hash;
            if (username == null) {
                findViewById(R.id.viewCircle).setVisibility(View.VISIBLE);
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
                status_text.setText("Please enter both username and password");
                return;
            }
            try {
                current_hash = sha256(password);
            } catch (NoSuchAlgorithmException e) {
                status_text.setText("Hashing Error");
                return;
            }
        }

        Log.d("GON_DEBUG : AUTH", "Sending request for mode: " + mode);
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("hash", current_hash);
        params.put("mode", mode);

        PreferenceManager.post("login.php", params, response_data -> {
            Log.d("GON_DEBUG : AUTH", "Response received for user: " + username);
            handle_auth_response(response_data, status_text, username, current_hash);
        });
    }

    private void handle_auth_response(String response_data, TextView status_text, String username, String current_hash) {
        try {
            org.json.JSONObject json = new org.json.JSONObject(response_data);
            String status = json.getString("status");
            String message = json.getString("message");

            if (status.equals("success")) {
                uuid = json.getString("uuid");

                PreferenceManager.save_uuid(this, uuid);
                PreferenceManager.save_username(this, username);

                if (json.has("profile_pic")) {
                    int pic_index = json.optInt("profile_pic", 0);
                    PreferenceManager.save_profile_pic(this, pic_index);
                }

                if (chk_remember_me != null && chk_remember_me.isChecked()) {
                    PreferenceManager.save_hash(this, current_hash);
                } else {
                    PreferenceManager.save_hash(this, null);
                }

                Intent intent = new Intent(MainActivity.this, GoalList.class);
                startActivity(intent);
                finish();
            } else {
                android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show();
                status_text.setText(message);
                
                findViewById(R.id.viewCircle).setVisibility(View.VISIBLE);
                findViewById(R.id.imageViewLogo).setVisibility(View.VISIBLE);
                findViewById(R.id.textSubtitle).setVisibility(View.VISIBLE);
                findViewById(R.id.loginCard).setVisibility(View.VISIBLE);
                findViewById(R.id.autoLoading).setVisibility(View.GONE);

                PreferenceManager.save_uuid(this, null);
                PreferenceManager.save_hash(this, null);
            }
        } catch (JSONException e) {
            android.widget.Toast.makeText(this, "Server Error: Invalid Response", android.widget.Toast.LENGTH_LONG).show();
            status_text.setText("Server Error: Invalid Response");
            Log.e("GON_DEBUG : AUTH", response_data);
        }
    }

    public String sha256(String raw_data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encoded_hash = digest.digest(raw_data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex_string = new StringBuilder(2 * encoded_hash.length);
        for (byte b : encoded_hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hex_string.append('0');
            hex_string.append(hex);
        }
        return hex_string.toString();
    }
}
