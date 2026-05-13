package com.example.dcfg_studyfi_casestuy;

public class Task {
    private int id;
    private int courseId; // Ito ang magko-connect ng task sa folder
    private String taskName;
    private boolean isCompleted;

    public Task(int id, int courseId, String taskName, boolean isCompleted) {
        this.id = id;
        this.courseId = courseId;
        this.taskName = taskName;
        this.isCompleted = isCompleted;
    }

    public int getId() { return id; }
    public int getCourseId() { return courseId; }
    public String getTaskName() { return taskName; }
    public boolean isCompleted() { return isCompleted; }

    public void setCompleted(boolean completed) { isCompleted = completed; }
}