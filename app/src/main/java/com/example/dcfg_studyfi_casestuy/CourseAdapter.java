package com.example.dcfg_studyfi_casestuy;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courseList;

    public CourseAdapter(List<Course> courseList) {
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course currentCourse = courseList.get(position);

        holder.tvCourseTitle.setText(currentCourse.getCourseName());

        try {
            holder.layoutBg.setBackgroundColor(Color.parseColor(currentCourse.getColorHex()));
        } catch (Exception e) {
            holder.layoutBg.setBackgroundColor(Color.GRAY);
        }

        // CLICK LISTENER: Kapag pinindot ang folder, bubuksan ang CourseDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CourseDetailActivity.class);
            // Ipapasa natin ang ID, Name, at Color para dynamic ang itsura ng next screen
            intent.putExtra("COURSE_ID", currentCourse.getId());
            intent.putExtra("COURSE_NAME", currentCourse.getCourseName());
            intent.putExtra("COURSE_COLOR", currentCourse.getColorHex());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseTitle;
        ConstraintLayout layoutBg;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseTitle = itemView.findViewById(R.id.tv_course_title);
            layoutBg = itemView.findViewById(R.id.layout_course_bg);
        }
    }
}