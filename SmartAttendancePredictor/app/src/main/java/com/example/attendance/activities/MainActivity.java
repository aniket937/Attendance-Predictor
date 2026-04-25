package com.example.attendance.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.attendance.R;
import com.example.attendance.adapter.SubjectAdapter;
import com.example.attendance.database.DatabaseHelper;
import com.example.attendance.model.Subject;
import com.example.attendance.utils.AttendanceUtils;
import com.example.attendance.utils.NotificationHelper;
import com.example.attendance.utils.SessionManager;
import com.example.attendance.utils.StorageHelper;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Main Activity - Displays list of subjects with attendance data.
 * Handles add, delete, and click actions on subjects.
 */
public class MainActivity extends AppCompatActivity implements SubjectAdapter.OnSubjectClickListener {

    private static final int REQUEST_ADD_SUBJECT = 100;
    private static final int REQUEST_EDIT_SUBJECT = 101;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 200;
    private static final int REQUEST_EXPORT_PERMISSION = 300;

    private TextView tvWelcome;
    private Button btnLogout;
    private RecyclerView recyclerView;
    private SubjectAdapter adapter;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private NotificationHelper notificationHelper;
    private StorageHelper storageHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize helpers
        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);
        notificationHelper = new NotificationHelper(this);
        storageHelper = new StorageHelper(this);

        // Setup toolbar with student name
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        // Initialize views
        initViews();

        // Request notification permission for Android 13+
        requestNotificationPermission();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogout = findViewById(R.id.btnLogout);

        // Display welcome message with student name
        String studentName = sessionManager.getStudentName();
        int rollNo = sessionManager.getRollNo();
        tvWelcome.setText(getString(R.string.welcome_message, studentName, rollNo));

        // Logout button click
        btnLogout.setOnClickListener(v -> handleLogout());

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup FAB
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddSubjectActivity.class);
            startActivityForResult(intent, REQUEST_ADD_SUBJECT);
        });

        // Load data
        loadSubjects();
    }

    private void loadSubjects() {
        List<Subject> subjects = dbHelper.getAllSubjects();
        adapter = new SubjectAdapter(this, subjects, this);
        recyclerView.setAdapter(adapter);

        // Check for low attendance and show notifications
        checkLowAttendance(subjects);
    }

    private void checkLowAttendance(List<Subject> subjects) {
        for (Subject subject : subjects) {
            double percentage = AttendanceUtils.calculateAttendance(
                    subject.getAttended(),
                    subject.getTotal()
            );
            if (percentage < AttendanceUtils.LOW_THRESHOLD) {
                notificationHelper.showLowAttendanceNotification(
                        subject.getName(),
                        percentage
                );
            }
        }
    }

    @Override
    public void onSubjectClick(Subject subject) {
        // Save last opened subject
        storageHelper.saveLastSubjectId(subject.getId());

        // Open prediction activity
        Intent intent = new Intent(this, PredictionActivity.class);
        intent.putExtra(PredictionActivity.EXTRA_SUBJECT_ID, subject.getId());
        startActivityForResult(intent, REQUEST_EDIT_SUBJECT);
    }

    @Override
    public void onSubjectLongClick(Subject subject) {
        // Show delete confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_subject)
                .setMessage(getString(R.string.delete_confirmation, subject.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    dbHelper.deleteSubject(subject.getId());
                    loadSubjects();
                    Toast.makeText(this, R.string.subject_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadSubjects();
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }
    }

    private void requestExportPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_EXPORT_PERMISSION);
            } else {
                exportData();
            }
        } else {
            exportData();
        }
    }

    private void exportData() {
        List<Subject> subjects = dbHelper.getAllSubjects();
        boolean success = storageHelper.exportToCSV(subjects);
        if (success) {
            Toast.makeText(this, R.string.export_success, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.export_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_logout)
                .setMessage(R.string.logout_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    sessionManager.logout();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXPORT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportData();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_export) {
            requestExportPermission();
            return true;
        } else if (id == R.id.action_logout) {
            handleLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}