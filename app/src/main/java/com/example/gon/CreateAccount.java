package com.example.gon;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CreateAccount extends AppCompatActivity {

    private EditText edtUsername, edtEmail, edtPassword;
    private TextView txtStatus;
    private CheckBox chkIsAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        edtUsername = findViewById(R.id.edtCreateUsername);
        edtEmail = findViewById(R.id.edtCreateEmail);
        edtPassword = findViewById(R.id.edtCreatePassword);
        txtStatus = findViewById(R.id.textViewStatus);
        chkIsAdmin = findViewById(R.id.chk_idAdmin);

        findViewById(R.id.txtLogin).setOnClickListener(v -> finish());
    }

    public void handleRegister(View v) {
        String username = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            txtStatus.setText("All fields are required");
            return;
        }

        try {
            String hash = sha256(password);
            boolean isAdmin = chkIsAdmin.isChecked();
            registerUserOnServer(username, email, hash, isAdmin);
        } catch (NoSuchAlgorithmException e) {
            txtStatus.setText("Error hashing password");
        }
    }

    private void registerUserOnServer(String username, String email, String hash, boolean is_admin) {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("email", email);
        params.put("hash", hash);
        params.put("is_admin", is_admin ? "1" : "0");
        params.put("mode", "create_account");

        PreferenceManager.post("login.php", params, responseData -> {
            handleServerResponse(responseData);
        });
    }

    private void handleServerResponse(String responseData) {
        try {
            JSONObject json = new JSONObject(responseData);
            String status = json.getString("status");
            String message = json.getString("message");

            if (status.equals("success")) {
                Toast.makeText(this, "Account Created! Please log in.", Toast.LENGTH_LONG).show();
                finish();
            } else {
                txtStatus.setText(message);
            }
        } catch (JSONException e) {
            txtStatus.setText("Server error");
            Log.e("REG_ERROR", responseData);
        }
    }

    private String sha256(String raw_data) throws NoSuchAlgorithmException {
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
