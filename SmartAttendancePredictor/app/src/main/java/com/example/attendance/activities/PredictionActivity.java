package com.example.attendance.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.attendance.R;
import com.example.attendance.database.DatabaseHelper;
import com.example.attendance.model.Subject;
import com.example.attendance.utils.AttendanceUtils;

/**
 * Activity for viewing and predicting attendance for a specific subject.
 * 
 * DATA SEPARATION:
 * - Original data (attended, total): NEVER modified by predictions
 * - Prediction data (tempAttended, tempTotal): Used only for calculations
 * 
 * The top UI always shows ORIGINAL attendance.
 * The prediction result shows TEMPORARY predicted attendance.
 */
public class PredictionActivity extends AppCompatActivity {

    public static final String EXTRA_SUBJECT_ID = "subject_id";

    // UI Elements - Original Attendance Display
    private TextView tvSubjectName;
    private TextView tvCurrentAttendance;
    private TextView tvStatus;
    private TextView tvMaxBunks;
    private ProgressBar progressBar;

    // UI Elements - Prediction
    private EditText etSkipCount;
    private EditText etAttendCount;
    private Button btnSkipMultiple;
    private Button btnAttendMultiple;
    private Button btnEditSubject;
    private TextView tvPrediction;

    private DatabaseHelper dbHelper;
    
    // ORIGINAL DATA - Do NOT modify these during predictions
    private Subject currentSubject;
    
    // PREDICTION DATA - Temporary variables (never saved to database)
    private int tempAttended;
    private int tempTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        dbHelper = DatabaseHelper.getInstance(this);
        initViews();
        loadSubjectData();
    }

    private void initViews() {
        // Original attendance UI elements
        tvSubjectName = findViewById(R.id.tvSubjectName);
        tvCurrentAttendance = findViewById(R.id.tvCurrentAttendance);
        tvStatus = findViewById(R.id.tvStatus);
        tvMaxBunks = findViewById(R.id.tvMaxBunks);
        progressBar = findViewById(R.id.progressBar);

        // Prediction UI elements
        etSkipCount = findViewById(R.id.etSkipCount);
        etAttendCount = findViewById(R.id.etAttendCount);
        btnSkipMultiple = findViewById(R.id.btnSkipMultiple);
        btnAttendMultiple = findViewById(R.id.btnAttendMultiple);
        btnEditSubject = findViewById(R.id.btnEditSubject);
        tvPrediction = findViewById(R.id.tvPrediction);

        // Initially, Prediction Result should be empty
        tvPrediction.setText("");

        btnSkipMultiple.setOnClickListener(v -> handleSkipMultiple());
        btnAttendMultiple.setOnClickListener(v -> handleAttendMultiple());
        btnEditSubject.setOnClickListener(v -> handleEditSubject());
    }

    private void loadSubjectData() {
        int subjectId = getIntent().getIntExtra(EXTRA_SUBJECT_ID, -1);
        if (subjectId == -1) {
            Toast.makeText(this, R.string.subject_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentSubject = dbHelper.getSubjectById(subjectId);
        if (currentSubject == null) {
            Toast.makeText(this, R.string.subject_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        updateOriginalUI();
    }

    /**
     * Update the ORIGINAL attendance UI
     * This shows the REAL data from database - NEVER modified by predictions
     */
    private void updateOriginalUI() {
        // Get original values (NEVER changed)
        int attended = currentSubject.getAttended();
        int total = currentSubject.getTotal();

        // Calculate original percentage
        double percentage = AttendanceUtils.calculateAttendance(attended, total);

        // Update top UI (original attendance display)
        tvSubjectName.setText(currentSubject.getName());
        tvCurrentAttendance.setText(AttendanceUtils.formatPercentage(percentage));
        tvStatus.setText(AttendanceUtils.getStatus(percentage));
        tvStatus.setTextColor(AttendanceUtils.getStatusColor(percentage));
        progressBar.setProgress((int) percentage);

        // Update maximum bunks section
        updateMaxBunksDisplay(percentage, attended, total);
    }

    /**
     * Update maximum bunks display based on original data
     */
    private void updateMaxBunksDisplay(double currentPercentage, int attended, int total) {
        if (currentPercentage >= AttendanceUtils.LOW_THRESHOLD) {
            // Calculate maximum bunks
            int maxBunks = calculateMaxBunks(attended, total);
            if (maxBunks > 0) {
                tvMaxBunks.setText(getString(R.string.max_bunks, maxBunks));
            } else {
                tvMaxBunks.setText(getString(R.string.max_bunks_zero));
            }
        } else {
            // Calculate classes needed to attend
            int classesNeeded = calculateClassesNeeded(attended, total);
            tvMaxBunks.setText(getString(R.string.need_attend, classesNeeded));
        }
    }

    /**
     * Calculate maximum number of classes that can be skipped while keeping attendance >= 75%
     * Uses TEMP variables - does NOT modify original data
     */
    private int calculateMaxBunks(int attended, int total) {
        if (total <= 0 || attended <= 0) {
            return 0;
        }

        int bunk = 0;
        while (true) {
            // Use temporary variables for calculation
            double projectedPercentage = (double) attended / (total + bunk) * 100;
            if (projectedPercentage >= AttendanceUtils.LOW_THRESHOLD) {
                bunk++;
            } else {
                break;
            }
            if (bunk > total + 100) {
                break;
            }
        }
        return Math.max(0, bunk - 1);
    }

    /**
     * Calculate minimum number of classes needed to attend to reach 75%
     * Uses TEMP variables - does NOT modify original data
     */
    private int calculateClassesNeeded(int attended, int total) {
        if (total <= 0) {
            return -1;
        }

        double currentPercentage = (double) attended / total * 100;
        if (currentPercentage >= AttendanceUtils.LOW_THRESHOLD) {
            return 0;
        }

        int classes = 0;
        while (true) {
            double projectedPercentage = (double) (attended + classes) / (total + classes) * 100;
            if (projectedPercentage >= AttendanceUtils.LOW_THRESHOLD) {
                break;
            }
            classes++;
            if (classes > 1000) {
                return -1;
            }
        }
        return classes;
    }

    /**
     * Calculate predicted attendance for SKIP
     * Formula: attended / (total + skipCount) * 100
     * Uses TEMP variables - does NOT modify original data
     */
    private double calculateSkipPrediction(int skipCount) {
        int originalAttended = currentSubject.getAttended();
        int originalTotal = currentSubject.getTotal();

        if (originalTotal <= 0 || skipCount < 0) {
            return 0.0;
        }

        // Use temporary values for prediction
        tempAttended = originalAttended;
        tempTotal = originalTotal + skipCount;

        return (double) tempAttended / tempTotal * 100;
    }

    /**
     * Calculate predicted attendance for ATTEND
     * Formula: (attended + attendCount) / (total + attendCount) * 100
     * Uses TEMP variables - does NOT modify original data
     */
    private double calculateAttendPrediction(int attendCount) {
        int originalAttended = currentSubject.getAttended();
        int originalTotal = currentSubject.getTotal();

        if (originalTotal < 0 || attendCount < 0) {
            return 0.0;
        }

        // Use temporary values for prediction
        tempAttended = originalAttended + attendCount;
        tempTotal = originalTotal + attendCount;

        return (double) tempAttended / tempTotal * 100;
    }

    /**
     * Handle Skip Multiple button click
     * ONLY calculates prediction - does NOT modify original data
     */
    private void handleSkipMultiple() {
        String skipCountStr = etSkipCount.getText().toString().trim();

        // Clear previous prediction result
        tvPrediction.setText("");

        // Validate empty input
        if (skipCountStr.isEmpty()) {
            etSkipCount.setError(getString(R.string.error_enter_count));
            etSkipCount.requestFocus();
            Toast.makeText(this, R.string.error_enter_count, Toast.LENGTH_SHORT).show();
            return;
        }

        int skipCount;
        try {
            skipCount = Integer.parseInt(skipCountStr);
        } catch (NumberFormatException e) {
            etSkipCount.setError(getString(R.string.error_invalid_count));
            etSkipCount.requestFocus();
            return;
        }

        if (skipCount <= 0) {
            etSkipCount.setError(getString(R.string.error_invalid_count));
            etSkipCount.requestFocus();
            return;
        }

        // Calculate predicted attendance using temporary values
        double predictedPercentage = calculateSkipPrediction(skipCount);
        String status = AttendanceUtils.getStatus(predictedPercentage);
        int statusColor = AttendanceUtils.getStatusColor(predictedPercentage);

        // Display prediction result (does NOT modify original UI)
        String resultText = getString(R.string.skip_prediction_result,
                skipCount,
                AttendanceUtils.formatPercentage(predictedPercentage),
                status);

        tvPrediction.setText(resultText);
        tvPrediction.setTextColor(statusColor);
    }

    /**
     * Handle Attend Multiple button click
     * ONLY calculates prediction - does NOT modify original data
     */
    private void handleAttendMultiple() {
        String attendCountStr = etAttendCount.getText().toString().trim();

        // Clear previous prediction result
        tvPrediction.setText("");

        // Validate empty input
        if (attendCountStr.isEmpty()) {
            etAttendCount.setError(getString(R.string.error_enter_count));
            etAttendCount.requestFocus();
            Toast.makeText(this, R.string.error_enter_count, Toast.LENGTH_SHORT).show();
            return;
        }

        int attendCount;
        try {
            attendCount = Integer.parseInt(attendCountStr);
        } catch (NumberFormatException e) {
            etAttendCount.setError(getString(R.string.error_invalid_count));
            etAttendCount.requestFocus();
            return;
        }

        if (attendCount <= 0) {
            etAttendCount.setError(getString(R.string.error_invalid_count));
            etAttendCount.requestFocus();
            return;
        }

        // Calculate predicted attendance using temporary values
        double predictedPercentage = calculateAttendPrediction(attendCount);
        String status = AttendanceUtils.getStatus(predictedPercentage);
        int statusColor = AttendanceUtils.getStatusColor(predictedPercentage);

        // Display prediction result (does NOT modify original UI)
        String resultText = getString(R.string.attend_prediction_result,
                attendCount,
                AttendanceUtils.formatPercentage(predictedPercentage),
                status);

        tvPrediction.setText(resultText);
        tvPrediction.setTextColor(statusColor);
    }

    private void handleEditSubject() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_subject)
                .setMessage(R.string.edit_subject_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> finish())
                .setNegativeButton(R.string.no, null)
                .show();
    }
}