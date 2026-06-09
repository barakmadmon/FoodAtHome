package com.example.foodathome;

import static android.app.PendingIntent.getActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity for user login and registration.
 */
public class LoginActivity extends AppCompatActivity {

    private boolean isLoginMode = true;

    private TextView tvTitle;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private Button btnAction;
    private MaterialButton btnSwitchMode;

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvTitle = findViewById(R.id.tv_title);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnAction = findViewById(R.id.btn_action);
        btnSwitchMode = findViewById(R.id.btn_switch_mode);

        btnSwitchMode.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            updateUI();
        });

        btnAction.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isLoginMode) {
                loginUser(username, password);
            } else {
                signinUser(username, password);
            }
        });
    }

    /**
     * Logs in a user with the given username and password.
     * @param username The username of the user.
     * @param password The password of the user.
     */
    private void loginUser(String username, String password) {
        FirebaseDataHandler.loginUser(username, password, result -> {
            if (result.equals("Success")) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Login failed: " + result, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Signs up a new user with the given username and password.
     * @param username The username of the new user.
     * @param password The password of the new user.
     */
    private void signinUser(String username, String password) {
        FirebaseDataHandler.signupUser(username, password, result -> {
            if (result.equals("Success")) {
                Toast.makeText(this, "Sign up successful! You can now login.", Toast.LENGTH_SHORT).show();
                isLoginMode = true;
                updateUI();
            } else {
                Toast.makeText(this, "Sign up failed: " + result, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the UI to reflect whether the user is in login or sign-up mode.
     */
    private void updateUI() {
        if (isLoginMode) {
            tvTitle.setText("Login");
            btnAction.setText("Login");
            btnSwitchMode.setText("Don't have an account? Sign Up");
        } else {
            tvTitle.setText("Create Account");
            btnAction.setText("Sign Up");
            btnSwitchMode.setText("Already have an account? Login");
        }
    }
}