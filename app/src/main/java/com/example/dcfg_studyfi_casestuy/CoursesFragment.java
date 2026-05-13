package com.example.dcfg_studyfi_casestuy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CoursesFragment extends Fragment {

    private RecyclerView rvCourses;
    private FloatingActionButton fabAddCourse;
    private CourseAdapter courseAdapter;
    private List<Course> courseList;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_courses, container, false);

        rvCourses = view.findViewById(R.id.rv_courses);
        fabAddCourse = view.findViewById(R.id.fab_add_course);
        dbHelper = new DatabaseHelper(getContext());
        courseList = new ArrayList<>();

        rvCourses.setLayoutManager(new GridLayoutManager(getContext(), 2));
        courseAdapter = new CourseAdapter(courseList);
        rvCourses.setAdapter(courseAdapter);

        loadCoursesFromDatabase();

        fabAddCourse.setOnClickListener(v -> showCustomAddCourseDialog());

        return view;
    }

    private void loadCoursesFromDatabase() {
        courseList.clear();
        courseList.addAll(dbHelper.getAllCourses());
        courseAdapter.notifyDataSetChanged();
    }

    private void showCustomAddCourseDialog() {
        // 1. I-load ang custom XML natin
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_course, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Para transparent ang background ng default dialog at masunod ang rounded corners (kung lalagyan sa future)
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // 2. I-link ang mga buttons at textfield mula sa dialog XML
        TextInputEditText etCourseName = dialogView.findViewById(R.id.et_dialog_course_name);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_dialog_save);

        // 3. Logic kapag pinindot ang Cancel
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // 4. Logic kapag pinindot ang Save
        btnSave.setOnClickListener(v -> {
            String courseName = etCourseName.getText().toString().trim();
            if (!courseName.isEmpty()) {
                String[] colors = {"#4CAF50", "#2196F3", "#FF9800", "#E91E63", "#9C27B0"};
                String randomColor = colors[(int) (Math.random() * colors.length)];

                boolean isInserted = dbHelper.addCourse(courseName, randomColor);
                if (isInserted) {
                    Toast.makeText(getContext(), "Course Added!", Toast.LENGTH_SHORT).show();
                    loadCoursesFromDatabase();
                    dialog.dismiss(); // I-close ang dialog pagkatapos mag-save
                } else {
                    Toast.makeText(getContext(), "Failed to add course.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Please enter a course name", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}