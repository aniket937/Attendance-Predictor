package com.example.attendance.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
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
 * Includes prediction buttons, countdown timer, and bunk warnings.
 */
public class PredictionActivity extends AppCompatActivity {

    public static final String EXTRA_SUBJECT_ID = "subject_id";

    private TextView tvSubjectName;
    private TextView tvCourseCode;
    private TextView tvCurrentAttendance;
    private TextView tvStatus;
    private TextView tvPrediction;
    private TextView tvMaxBunks;
    private ProgressBar progressBar;

    private EditText etSkipCount;
    private EditText etTimerMinutes;
    private Button btnAttendNext;
    private Button btnSkipNext;
    private Button btnSkipMultiple;
    private Button btnStartTimer;
    private Button btnEditSubject;

    private TextView tvTimer;
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;

    private DatabaseHelper dbHelper;
    private Subject currentSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        // Initialize database helper
        dbHelper = DatabaseHelper.getInstance(this);

        // Initialize views
        initViews();

        // Load subject data
        loadSubjectData();
    }

    private void initViews() {
        tvSubjectName = findViewById(R.id.tvSubjectName);
        tvCourseCode = findViewById(R.id.tvCourseCode);
        tvCurrentAttendance = findViewById(R.id.tvCurrentAttendance);
        tvStatus = findViewById(R.id.tvStatus);
        tvPrediction = findViewById(R.id.tvPrediction);
        tvMaxBunks = findViewById(R.id.tvMaxBunks);
        progressBar = findViewById(R.id.progressBar);

        etSkipCount = findViewById(R.id.etSkipCount);
        etTimerMinutes = findViewById(R.id.etTimerMinutes);
        btnAttendNext = findViewById(R.id.btnAttendNext);
        btnSkipNext = findViewById(R.id.btnSkipNext);
        btnSkipMultiple = findViewById(R.id.btnSkipMultiple);
        btnStartTimer = findViewById(R.id.btnStartTimer);
        btnEditSubject = findViewById(R.id.btnEditSubject);

        tvTimer = findViewById(R.id.tvTimer);

        // Set click listeners
        btnAttendNext.setOnClickListener(v -> handleAttendNext());
        btnSkipNext.setOnClickListener(v -> handleSkipNext());
        btnSkipMultiple.setOnClickListener(v -> handleSkipMultiple());
        btnStartTimer.setOnClickListener(v -> handleTimer());
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

        updateUI();
    }

    private void updateUI() {
        double percentage = AttendanceUtils.calculateAttendance(
                currentSubject.getAttended(),
                currentSubject.getTotal()
        );

        tvSubjectName.setText(currentSubject.getName());

        if (currentSubject.getCourseCode() != null && !currentSubject.getCourseCode().isEmpty()) {
            tvCourseCode.setText(currentSubject.getCourseCode());
            tvCourseCode.setVisibility(TextView.VISIBLE);
        } else {
            tvCourseCode.setVisibility(TextView.GONE);
        }

        tvCurrentAttendance.setText(AttendanceUtils.formatPercentage(percentage));
        tvStatus.setText(AttendanceUtils.getStatus(percentage));
        tvStatus.setTextColor(AttendanceUtils.getStatusColor(percentage));

        // Update progress bar
        progressBar.setProgress((int) percentage);

        // Calculate max bunks
        int maxBunks = AttendanceUtils.maxBunks(
                currentSubject.getAttended(),
                currentSubject.getTotal(),
                AttendanceUtils.LOW_THRESHOLD
        );
        tvMaxBunks.setText(getString(R.string.max_bunks, maxBunks));
    }

    private void handleAttendNext() {
        double newPercentage = AttendanceUtils.attendNext(
                currentSubject.getAttended(),
                currentSubject.getTotal()
        );

        tvPrediction.setText(getString(R.string.attend_next_prediction,
                AttendanceUtils.formatPercentage(newPercentage)));

        // Ask for confirmation
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_attend)
                .setMessage(R.string.confirm_attend_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    currentSubject.setTotal(currentSubject.getTotal() + 1);
                    currentSubject.setAttended(currentSubject.getAttended() + 1);
                    saveAndRefresh();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void handleSkipNext() {
        double currentPercentage = AttendanceUtils.calculateAttendance(
                currentSubject.getAttended(),
                currentSubject.getTotal()
        );

        // Show warning if attendance is low
        if (currentPercentage < AttendanceUtils.LOW_THRESHOLD) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.warning)
                    .setMessage(R.string.low_attendance_warning)
                    .setPositiveButton(R.string.skip_anyway, (dialog, which) -> {
                        applySkipNext();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            double newPercentage = AttendanceUtils.skipNext(
                    currentSubject.getAttended(),
                    currentSubject.getTotal()
            );
            tvPrediction.setText(getString(R.string.skip_next_prediction,
                    AttendanceUtils.formatPercentage(newPercentage)));

            applySkipNext();
        }
    }

    private void applySkipNext() {
        currentSubject.setTotal(currentSubject.getTotal() + 1);
        saveAndRefresh();
    }

    private void handleSkipMultiple() {
        String skipCountStr = etSkipCount.getText().toString().trim();

        if (skipCountStr.isEmpty()) {
            Toast.makeText(this, R.string.enter_skip_count, Toast.LENGTH_SHORT).show();
            return;
        }

        int skipCount;
        try {
            skipCount = Integer.parseInt(skipCountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.invalid_number, Toast.LENGTH_SHORT).show();
            return;
        }

        if (skipCount <= 0) {
            Toast.makeText(this, R.string.invalid_skip_count, Toast.LENGTH_SHORT).show();
            return;
        }

        double newPercentage = AttendanceUtils.skipMultiple(
                currentSubject.getAttended(),
                currentSubject.getTotal(),
                skipCount
        );

        tvPrediction.setText(getString(R.string.skip_multiple_prediction,
                skipCount, AttendanceUtils.formatPercentage(newPercentage)));

        // Show warning dialog
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_skip_multiple)
                .setMessage(getString(R.string.confirm_skip_multiple_message, skipCount))
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    currentSubject.setTotal(currentSubject.getTotal() + skipCount);
                    saveAndRefresh();
                    etSkipCount.setText("");
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void handleTimer() {
        if (isTimerRunning) {
            // Stop timer
            countDownTimer.cancel();
            isTimerRunning = false;
            btnStartTimer.setText(R.string.start_timer);
            tvTimer.setText(etTimerMinutes.getText().toString().trim() + ":00");
        } else {
            // Get user entered minutes
            String minutesStr = etTimerMinutes.getText().toString().trim();
            int minutes;

            try {
                minutes = Integer.parseInt(minutesStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.invalid_timer_value, Toast.LENGTH_SHORT).show();
                return;
            }

            if (minutes <= 0 || minutes > 180) {
                Toast.makeText(this, R.string.invalid_timer_value, Toast.LENGTH_SHORT).show();
                return;
            }

            // Start countdown timer with user-defined duration
            countDownTimer = new CountDownTimer(minutes * 60 * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long mins = millisUntilFinished / 60000;
                    long secs = (millisUntilFinished % 60000) / 1000;
                    tvTimer.setText(String.format("%02d:%02d", mins, secs));
                }

                @Override
                public void onFinish() {
                    tvTimer.setText(R.string.timer_finished);
                    Toast.makeText(PredictionActivity.this, R.string.study_session_done,
                            Toast.LENGTH_SHORT).show();
                    isTimerRunning = false;
                    btnStartTimer.setText(R.string.start_timer);
                }
            };
            countDownTimer.start();
            isTimerRunning = true;
            btnStartTimer.setText(R.string.stop_timer);
        }
    }

    private void handleEditSubject() {
        // This will be handled by opening AddSubjectActivity with edit mode
        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_subject)
                .setMessage(R.string.edit_subject_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void saveAndRefresh() {
        boolean success = dbHelper.updateSubject(currentSubject);
        if (success) {
            updateUI();
        } else {
            Toast.makeText(this, R.string.update_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}