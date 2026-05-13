package com.example.dcfg_studyfi_casestuy;

public class Course {
    // Variables para sa properties ng isang Course (Folder)
    private int id;
    private String courseName;
    private String colorHex; // Dito natin ise-save ang kulay (e.g., "#FF5733")

    // Constructor: Ito ang ginagamit para gumawa ng bagong Course object
    public Course(int id, String courseName, String colorHex) {
        this.id = id;
        this.courseName = courseName;
        this.colorHex = colorHex;
    }

    // Getters: Para makuha ang mga values sa ibang classes (tulad ng sa Adapter)
    public int getId() {
        return id;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getColorHex() {
        return colorHex;
    }
}