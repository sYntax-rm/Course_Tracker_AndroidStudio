package com.example.dcfg_studyfi_casestuy;

// MGA IMPORT LIBRARIES (Dito tayo nagka-error kanina, kulang ang Intent)
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class CourseDetailActivity extends AppCompatActivity {

    private int courseId;
    private String courseName;
    private String courseColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        // 1. Kunin ang impormasyon na ipinasa galing sa clicked folder
        courseId = getIntent().getIntExtra("COURSE_ID", -1);
        courseName = getIntent().getStringExtra("COURSE_NAME");
        courseColor = getIntent().getStringExtra("COURSE_COLOR");

        // 2. I-link ang mga Views
        LinearLayout headerLayout = findViewById(R.id.header_course_detail);
        TextView tvCourseName = findViewById(R.id.tv_detail_course_name);
        ImageButton btnBack = findViewById(R.id.btn_back);

        CardView cardTasks = findViewById(R.id.card_tasks);
        CardView cardPdfs = findViewById(R.id.card_pdfs);
        CardView cardLinks = findViewById(R.id.card_links);

        // 3. I-apply ang dynamic data sa UI
        if (courseName != null) {
            tvCourseName.setText(courseName);
        }

        try {
            if (courseColor != null) {
                headerLayout.setBackgroundColor(Color.parseColor(courseColor));
            }
        } catch (Exception e) {
            headerLayout.setBackgroundColor(Color.GRAY);
        }

        // 4. Back Button Logic
        btnBack.setOnClickListener(v -> finish());

        // 5. Setup Submodule Clicks
        cardTasks.setOnClickListener(v -> {
            // Ngayong may import na, gagana na itong Intent!
            Intent intent = new Intent(CourseDetailActivity.this, CourseTasksActivity.class);
            intent.putExtra("COURSE_ID", courseId);
            intent.putExtra("COURSE_NAME", courseName);
            intent.putExtra("COURSE_COLOR", courseColor);
            startActivity(intent);
        });

        cardPdfs.setOnClickListener(v -> {
            Toast.makeText(this, "Opening PDF Manager for: " + courseName, Toast.LENGTH_SHORT).show();
        });

        cardLinks.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Links Manager for: " + courseName, Toast.LENGTH_SHORT).show();
        });
    }
}