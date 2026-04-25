package com.example.attendance.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.attendance.R;
import com.example.attendance.database.DatabaseHelper;
import com.example.attendance.model.Student;

/**
 * RegisterActivity - Student registration screen.
 * Validates inputs and saves student to database.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etBranch;
    private EditText etRollNo;
    private Button btnRegister;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = DatabaseHelper.getInstance(this);

        initViews();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etBranch = findViewById(R.id.etBranch);
        etRollNo = findViewById(R.id.etRollNo);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> handleRegister());
    }

    private void handleRegister() {
        String name = etName.getText().toString().trim();
        String branch = etBranch.getText().toString().trim();
        String rollNoStr = etRollNo.getText().toString().trim();

        // Validate Name
        if (name.isEmpty()) {
            etName.setError(getString(R.string.error_enter_name));
            etName.requestFocus();
            return;
        }

        // Validate Branch
        if (branch.isEmpty()) {
            etBranch.setError(getString(R.string.error_enter_branch));
            etBranch.requestFocus();
            return;
        }

        // Validate Roll Number
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

        // Check if roll number already exists
        if (dbHelper.checkStudentExists(rollNo)) {
            etRollNo.setError(getString(R.string.error_roll_no_exists));
            etRollNo.requestFocus();
            return;
        }

        // Register student
        Student student = new Student(rollNo, name, branch);
        boolean success = dbHelper.registerStudent(student);

        if (success) {
            Toast.makeText(this, R.string.registration_success, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, R.string.registration_failed, Toast.LENGTH_SHORT).show();
        }
    }
}