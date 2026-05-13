package com.example.dcfg_studyfi_casestuy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etName, etCourse, etSchool;
    private MaterialButton btnStart;
    private ProgressBar loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // CHECKER: Kapag may nakasave na sa SharedPreferences, wag na ipakita itong setup screen.
        SharedPreferences prefs = getSharedPreferences("StudyFiPrefs", MODE_PRIVATE);
        boolean isSetupDone = prefs.getBoolean("isSetupDone", false);

        if (isSetupDone) {
            // Diretso agad sa HomeActivity
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
            return; // Itigil na ang pag-run ng code sa ibaba
        }

        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.et_name);
        etCourse = findViewById(R.id.et_course);
        etSchool = findViewById(R.id.et_school);
        btnStart = findViewById(R.id.btn_start);
        loadingProgress = findViewById(R.id.loading_progress);

        btnStart.setOnClickListener(v -> processUserInputs());
    }

    private void processUserInputs() {
        String name = etName.getText().toString().trim();
        String course = etCourse.getText().toString().trim();
        String school = etSchool.getText().toString().trim();

        if (name.isEmpty() || course.isEmpty() || school.isEmpty()) {
            Toast.makeText(this, "Fill up mo muna lahat, lodi!", Toast.LENGTH_SHORT).show();
            return;
        }

        startLoadingUI();

        new Handler().postDelayed(() -> {
            // SAVE TO SHAREDPREFERENCES: Imbes na Intent Extras lang, i-save natin ito sa device storage
            SharedPreferences prefs = getSharedPreferences("StudyFiPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isSetupDone", true);
            editor.putString("USER_NAME", name);
            editor.putString("USER_COURSE", course);
            editor.putString("USER_SCHOOL", school);
            editor.apply(); // I-save ng tuluyan

            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2000);
    }

    private void startLoadingUI() {
        btnStart.setText("");
        btnStart.setEnabled(false);
        loadingProgress.setVisibility(View.VISIBLE);
    }
}