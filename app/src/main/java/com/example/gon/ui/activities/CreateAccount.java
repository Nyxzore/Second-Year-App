package com.example.gon.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gon.R;
import com.example.gon.utils.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CreateAccount extends AppCompatActivity {

    private EditText edt_username, edt_email, edt_password;
    private TextView txt_status;
    private CheckBox chk_is_admin;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.activity_create_account);
        Log.d("GON_DEBUG : REGISTER", "Activity created");

        edt_username = findViewById(R.id.edtCreateUsername);
        edt_email = findViewById(R.id.edtCreateEmail);
        edt_password = findViewById(R.id.edtCreatePassword);
        txt_status = findViewById(R.id.textViewStatus);
        chk_is_admin = findViewById(R.id.chk_idAdmin);

        findViewById(R.id.txtLogin).setOnClickListener(v -> finish());
    }

    public void handle_register(View v) {
        String username = edt_username.getText().toString().trim();
        String email = edt_email.getText().toString().trim();
        String password = edt_password.getText().toString().trim();

        Log.d("GON_DEBUG : REGISTER", "Register button clicked for: " + username);

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            txt_status.setText("All fields are required");
            return;
        }

        try {
            String hash = sha256(password);
            boolean is_admin = chk_is_admin.isChecked();
            register_user_on_server(username, email, hash, is_admin);
        } catch (NoSuchAlgorithmException e) {
            Log.e("GON_DEBUG : REGISTER", "Hashing error", e);
            txt_status.setText("Error hashing password");
        }
    }

    private void register_user_on_server(String username, String email, String hash, boolean is_admin) {
        Log.d("GON_DEBUG : REGISTER", "Sending registration request for: " + username);
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("email", email);
        params.put("hash", hash);
        params.put("is_admin", is_admin ? "1" : "0");
        params.put("mode", "create_account");

        PreferenceManager.post("login.php", params, this::handle_server_response);
    }

    private void handle_server_response(String response_data) {
        Log.d("GON_DEBUG : REGISTER", "Server response: " + response_data);
        try {
            JSONObject json = new JSONObject(response_data);
            String status = json.getString("status");
            String message = json.getString("message");

            if (status.equals("success")) {
                Log.d("GON_DEBUG : REGISTER", "Registration successful");
                Toast.makeText(this, "Account Created! Please log in.", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Log.d("GON_DEBUG : REGISTER", "Registration failed: " + message);
                txt_status.setText(message);
            }
        } catch (JSONException e) {
            txt_status.setText("Server error");
            Log.e("GON_DEBUG : REGISTER", "JSON parse error", e);
        }
    }

    private String sha256(String raw_data) throws NoSuchAlgorithmException {
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
