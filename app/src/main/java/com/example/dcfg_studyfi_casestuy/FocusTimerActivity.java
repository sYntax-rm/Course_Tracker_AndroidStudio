package com.example.dcfg_studyfi_casestuy;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * FocusTimerActivity — Pomodoro-style study timer
 *
 * Modes:  STUDY (default 25 min) → BREAK (5 min) → LONG BREAK (15 min)
 * Features:
 *  - Start / Pause / Reset / Skip
 *  - Auto-switch between study and break
 *  - Session counter + minutes tracked
 *  - Custom duration input
 *  - Progress ring via TimerProgressView
 */
public class FocusTimerActivity extends AppCompatActivity {

    private static final int MODE_STUDY = 0;
    private static final int MODE_BREAK = 1;
    private static final int MODE_LONG  = 2;

    private long studyDurationMs  = 25 * 60 * 1000L;
    private long breakDurationMs  =  5 * 60 * 1000L;
    private long longBreakDurationMs = 15 * 60 * 1000L;

    private int  currentMode      = MODE_STUDY;
    private boolean isRunning     = false;
    private int  sessionsCompleted = 0;
    private int  totalMinutesFocused = 0;
    private int  goalSessions     = 4;
    private long timeLeftMs;
    private long totalDurationMs;

    private CountDownTimer countDownTimer;

    // ── Views ──────────────────────────────────────────────────────────────
    private TextView        tvTimer, tvTimerLabel;
    private TextView        tvSessionCount, tvMinutesFocused, tvGoalCount;
    private Button          btnStartPause, btnModeStudy, btnModeBreak, btnModeLong;
    private ImageButton     btnReset, btnSkip;
    private TimerProgressView timerProgressView;
    private View            progressBarFill;
    private EditText        etStudyDuration, etBreakDuration;
    private Button          btnApplyDuration;

    private static final int COLOR_STUDY = Color.parseColor("#6C8EFF");
    private static final int COLOR_BREAK = Color.parseColor("#4ECDC4");
    private static final int COLOR_LONG  = Color.parseColor("#FF6B6B");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_timer);

        bindViews();
        setupModeButtons();
        setupControlButtons();
        setupCustomDuration();

        resetTimer(false);
    }

    // ── View binding ───────────────────────────────────────────────────────
    private void bindViews() {
        tvTimer            = findViewById(R.id.tvTimer);
        tvTimerLabel       = findViewById(R.id.tvTimerLabel);
        tvSessionCount     = findViewById(R.id.tvSessionCount);
        tvMinutesFocused   = findViewById(R.id.tvMinutesFocused);
        tvGoalCount        = findViewById(R.id.tvGoalCount);
        btnStartPause      = findViewById(R.id.btnStartPause);
        btnModeStudy       = findViewById(R.id.btnModeStudy);
        btnModeBreak       = findViewById(R.id.btnModeBreak);
        btnModeLong        = findViewById(R.id.btnModeLong);
        btnReset           = findViewById(R.id.btnReset);
        btnSkip            = findViewById(R.id.btnSkip);
        timerProgressView  = findViewById(R.id.timerProgressView);
        progressBarFill    = findViewById(R.id.progressBarFill);
        etStudyDuration    = findViewById(R.id.etStudyDuration);
        etBreakDuration    = findViewById(R.id.etBreakDuration);
        btnApplyDuration   = findViewById(R.id.btnApplyDuration);
    }


    private void setupModeButtons() {
        btnModeStudy.setOnClickListener(v -> switchMode(MODE_STUDY));
        btnModeBreak.setOnClickListener(v -> switchMode(MODE_BREAK));
        btnModeLong .setOnClickListener(v -> switchMode(MODE_LONG));
    }

    private void switchMode(int mode) {
        if (isRunning) pauseTimer();
        currentMode = mode;
        updateModeUI();
        resetTimer(false);
    }

    private void updateModeUI() {
        // Reset all tab styles
        btnModeStudy.setTextColor(Color.parseColor("#8A8FA8"));
        btnModeBreak.setTextColor(Color.parseColor("#8A8FA8"));
        btnModeLong .setTextColor(Color.parseColor("#8A8FA8"));
        btnModeStudy.setBackgroundResource(android.R.color.transparent);
        btnModeBreak.setBackgroundResource(android.R.color.transparent);
        btnModeLong .setBackgroundResource(android.R.color.transparent);

        int accentColor;
        String label;

        switch (currentMode) {
            case MODE_BREAK:
                btnModeBreak.setTextColor(Color.WHITE);
                accentColor = COLOR_BREAK;
                label = "SHORT BREAK";
                break;
            case MODE_LONG:
                btnModeLong.setTextColor(Color.WHITE);
                accentColor = COLOR_LONG;
                label = "LONG BREAK";
                break;
            default: // STUDY
                btnModeStudy.setTextColor(Color.WHITE);
                accentColor = COLOR_STUDY;
                label = "STUDY SESSION";
                break;
        }

        tvTimerLabel.setTextColor(accentColor);
        tvTimerLabel.setText(label);
        if (timerProgressView != null) {
            timerProgressView.setProgressColor(accentColor);
        }
    }

    // ── Control buttons
    private void setupControlButtons() {
        btnStartPause.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnReset.setOnClickListener(v -> {
            pauseTimer();
            resetTimer(false);
        });

        btnSkip.setOnClickListener(v -> {
            pauseTimer();
            onSessionComplete(false); // skip = no credit
        });
    }

    // ── Timer logic
    private void startTimer() {
        if (timeLeftMs <= 0) resetTimer(false);

        isRunning = true;
        btnStartPause.setText("PAUSE");

        countDownTimer = new CountDownTimer(timeLeftMs, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMs = millisUntilFinished;
                updateTimerDisplay();
                updateProgressRing();
            }

            @Override
            public void onFinish() {
                timeLeftMs = 0;
                updateTimerDisplay();
                updateProgressRing();
                isRunning = false;
                onSessionComplete(true);
            }
        }.start();
    }

    private void pauseTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        isRunning = false;
        btnStartPause.setText("START");
    }

    private void resetTimer(boolean autoStart) {
        pauseTimer();

        switch (currentMode) {
            case MODE_BREAK: totalDurationMs = breakDurationMs;     break;
            case MODE_LONG:  totalDurationMs = longBreakDurationMs; break;
            default:         totalDurationMs = studyDurationMs;     break;
        }

        timeLeftMs = totalDurationMs;
        updateTimerDisplay();
        updateProgressRing();

        if (autoStart) startTimer();
    }

    private void onSessionComplete(boolean countIt) {
        if (countIt && currentMode == MODE_STUDY) {
            sessionsCompleted++;
            totalMinutesFocused += (int)(studyDurationMs / 60000);
            updateStatsUI();
            Toast.makeText(this, "Session done! Take a break 🎉", Toast.LENGTH_SHORT).show();

            // Auto switch to break
            currentMode = (sessionsCompleted % 4 == 0) ? MODE_LONG : MODE_BREAK;
        } else if (countIt) {
            Toast.makeText(this, "Break over. Back to work! 💪", Toast.LENGTH_SHORT).show();
            currentMode = MODE_STUDY;
        } else {
            // Skipped — just move to next
            currentMode = (currentMode == MODE_STUDY)
                    ? MODE_BREAK
                    : MODE_STUDY;
        }

        updateModeUI();
        resetTimer(false); // auto-start disabled; user taps START
    }

    private void updateTimerDisplay() {
        long minutes = timeLeftMs / 60000;
        long seconds = (timeLeftMs % 60000) / 1000;
        tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void updateProgressRing() {
        float progress = (totalDurationMs > 0)
                ? 1f - ((float) timeLeftMs / totalDurationMs)
                : 0f;

        if (timerProgressView != null) {
            timerProgressView.setProgress(progress);
        }

        // Also update the linear progress bar in stats card
        if (progressBarFill != null && progressBarFill.getParent() instanceof View) {
            View track = (View) progressBarFill.getParent();
            track.post(() -> {
                int trackWidth = track.getWidth();
                float sessionProgress = (goalSessions > 0)
                        ? (float) sessionsCompleted / goalSessions
                        : 0f;
                int fillWidth = (int)(trackWidth * Math.min(sessionProgress, 1f));
                progressBarFill.getLayoutParams().width = fillWidth;
                progressBarFill.requestLayout();
            });
        }
    }

    private void updateStatsUI() {
        tvSessionCount.setText(String.valueOf(sessionsCompleted));
        tvMinutesFocused.setText(String.valueOf(totalMinutesFocused));
        tvGoalCount.setText(String.valueOf(goalSessions));
    }

    // ── Custom duration
    private void setupCustomDuration() {
        btnApplyDuration.setOnClickListener(v -> {
            try {
                String studyStr = etStudyDuration.getText().toString().trim();
                String breakStr = etBreakDuration.getText().toString().trim();

                if (!studyStr.isEmpty()) {
                    int studyMin = Integer.parseInt(studyStr);
                    if (studyMin < 1 || studyMin > 120) {
                        Toast.makeText(this, "Study duration must be 1–120 min", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    studyDurationMs = studyMin * 60 * 1000L;
                }

                if (!breakStr.isEmpty()) {
                    int breakMin = Integer.parseInt(breakStr);
                    if (breakMin < 1 || breakMin > 60) {
                        Toast.makeText(this, "Break duration must be 1–60 min", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    breakDurationMs = breakMin * 60 * 1000L;
                }

                pauseTimer();
                resetTimer(false);
                Toast.makeText(this, "Duration updated!", Toast.LENGTH_SHORT).show();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Lifecycle
    @Override
    protected void onPause() {
        super.onPause();
        // Keep timer state but pause to save battery
        if (isRunning) pauseTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
