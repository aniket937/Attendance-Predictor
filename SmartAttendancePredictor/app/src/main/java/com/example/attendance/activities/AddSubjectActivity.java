package com.example.attendance.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.attendance.R;
import com.example.attendance.database.DatabaseHelper;
import com.example.attendance.model.Subject;
import com.example.attendance.utils.AttendanceUtils;

/**
 * Activity for adding a new subject or editing an existing one.
 * Handles input validation and database operations.
 */
public class AddSubjectActivity extends AppCompatActivity {

    public static final String EXTRA_SUBJECT_ID = "subject_id";

    private EditText etSubjectName;
    private EditText etTotalClasses;
    private EditText etClassesAttended;
    private Button btnSave;

    private DatabaseHelper dbHelper;
    private int editSubjectId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_subject);

        // Initialize database helper
        dbHelper = DatabaseHelper.getInstance(this);

        // Initialize views
        initViews();

        // Check if editing existing subject
        editSubjectId = getIntent().getIntExtra(EXTRA_SUBJECT_ID, -1);
        if (editSubjectId != -1) {
            loadSubjectData();
        }
    }

    private void initViews() {
        etSubjectName = findViewById(R.id.etSubjectName);
        etTotalClasses = findViewById(R.id.etTotalClasses);
        etClassesAttended = findViewById(R.id.etClassesAttended);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> saveSubject());
    }

    private void loadSubjectData() {
        Subject subject = dbHelper.getSubjectById(editSubjectId);
        if (subject != null) {
            etSubjectName.setText(subject.getName());
            etTotalClasses.setText(String.valueOf(subject.getTotal()));
            etClassesAttended.setText(String.valueOf(subject.getAttended()));

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.edit_subject);
            }
        }
    }

    private void saveSubject() {
        String name = etSubjectName.getText().toString().trim();
        String totalStr = etTotalClasses.getText().toString().trim();
        String attendedStr = etClassesAttended.getText().toString().trim();

        // Validate input
        String error = AttendanceUtils.validateInput(name, totalStr, attendedStr);
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }

        int total = Integer.parseInt(totalStr);
        int attended = Integer.parseInt(attendedStr);

        Subject subject;
        if (editSubjectId != -1) {
            subject = new Subject(editSubjectId, name, total, attended);
            boolean success = dbHelper.updateSubject(subject);
            if (success) {
                Toast.makeText(this, R.string.subject_updated, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, R.string.update_failed, Toast.LENGTH_SHORT).show();
            }
        } else {
            subject = new Subject(name, total, attended);
            boolean success = dbHelper.insertSubject(subject);
            if (success) {
                Toast.makeText(this, R.string.subject_added, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, R.string.insert_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }
}