package com.example.attendance.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.attendance.R;
import com.example.attendance.database.DatabaseHelper;
import com.example.attendance.model.Student;
import com.example.attendance.utils.SessionManager;

/**
 * LoginActivity - Student login screen.
 * Validates roll number and opens MainActivity on success.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etRollNo;
    private Button btnLogin;
    private TextView tvRegister;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize helpers
        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        initViews();
    }

    private void initViews() {
        etRollNo = findViewById(R.id.etRollNo);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> handleLogin());
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String rollNoStr = etRollNo.getText().toString().trim();

        // Validate input
        if (rollNoStr.isEmpty()) {
            etRollNo.setError(getString(R.string.error_enter_roll_no));
            etRollNo.requestFocus();
            return;
        }

        int rollNo;
        try {
            rollNo = Integer.parseInt(rollNoStr);
        } catch (NumberFormatException e) {
            etRollNo.setError(getString(R.string.error_invalid_roll_no));
            etRollNo.requestFocus();
            return;
        }

        // Check if student exists
        Student student = dbHelper.getStudentByRollNo(rollNo);
        if (student == null) {
            Toast.makeText(this, R.string.error_not_registered, Toast.LENGTH_SHORT).show();
            return;
        }

        // Create session
        sessionManager.createLoginSession(student.getRollNo(), student.getName(), student.getBranch());

        // Open MainActivity
        Toast.makeText(this, getString(R.string.login_success, student.getName()), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}