package com.example.dcfg_studyfi_casestuy;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CourseTasksActivity extends AppCompatActivity {

    private int courseId;
    private String courseName;
    private String courseColor;

    private DatabaseHelper dbHelper;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;

    private RecyclerView rvTasks;
    private EditText etNewTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_tasks);

        // Kunin ang Intent data
        courseId = getIntent().getIntExtra("COURSE_ID", -1);
        courseName = getIntent().getStringExtra("COURSE_NAME");
        courseColor = getIntent().getStringExtra("COURSE_COLOR");

        // Initialize Views
        LinearLayout header = findViewById(R.id.header_tasks);
        TextView tvTitle = findViewById(R.id.tv_task_course_title);
        ImageButton btnBack = findViewById(R.id.btn_back_tasks);
        rvTasks = findViewById(R.id.rv_course_tasks);
        etNewTask = findViewById(R.id.et_new_task);
        FloatingActionButton fabSaveTask = findViewById(R.id.fab_save_task);

        dbHelper = new DatabaseHelper(this);
        taskList = new ArrayList<>();

        // Apply Custom Header Design
        if (courseName != null) tvTitle.setText(courseName + " Tasks");
        try {
            if (courseColor != null) header.setBackgroundColor(Color.parseColor(courseColor));
        } catch (Exception ignored) {}

        // Setup RecyclerView
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList, new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskStatusChanged(Task task, boolean isCompleted) {
                dbHelper.updateTaskStatus(task.getId(), isCompleted);
                loadTasks(); // I-refresh ang list para lumabas ang strikethrough
            }

            @Override
            public void onTaskDeleted(Task task) {
                dbHelper.deleteTask(task.getId());
                Toast.makeText(CourseTasksActivity.this, "Task Deleted", Toast.LENGTH_SHORT).show();
                loadTasks(); // I-refresh ang list
            }
        });
        rvTasks.setAdapter(taskAdapter);

        loadTasks();

        // Button Listeners
        btnBack.setOnClickListener(v -> finish());

        fabSaveTask.setOnClickListener(v -> {
            String taskName = etNewTask.getText().toString().trim();
            if (!taskName.isEmpty() && courseId != -1) {
                boolean isSaved = dbHelper.addTask(courseId, taskName);
                if (isSaved) {
                    etNewTask.setText(""); // Linisin ang input field
                    loadTasks();
                } else {
                    Toast.makeText(this, "Failed to save task", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadTasks() {
        taskList.clear();
        taskList.addAll(dbHelper.getTasksForCourse(courseId));
        taskAdapter.notifyDataSetChanged();
    }
}