package com.example.dcfg_studyfi_casestuy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView tvGreeting = findViewById(R.id.tv_greeting);
        TextView tvIdName = findViewById(R.id.tv_id_name);
        TextView tvIdCourse = findViewById(R.id.tv_id_course);
        TextView tvIdSchool = findViewById(R.id.tv_id_school);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // KUNIN ANG DATA SA SHAREDPREFERENCES (Hindi na sa Intent)
        SharedPreferences prefs = getSharedPreferences("StudyFiPrefs", MODE_PRIVATE);
        String name = prefs.getString("USER_NAME", "Student");
        String course = prefs.getString("USER_COURSE", "Course");
        String school = prefs.getString("USER_SCHOOL", "School");

        tvGreeting.setText(getString(R.string.hello_placeholder, name));
        tvIdName.setText(name.toUpperCase(Locale.ROOT));
        tvIdCourse.setText(course.toUpperCase(Locale.ROOT));
        tvIdSchool.setText(school.toUpperCase(Locale.ROOT));

        // Default screen ay CoursesFragment na
        if (savedInstanceState == null) {
            loadFragment(new CoursesFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                loadFragment(new TodoFragment());
                return true;
            } else if (id == R.id.nav_courses) {
                loadFragment(new CoursesFragment());
                return true;
            }else if (id == R.id.nav_timer){
                Intent intent = new Intent(HomeActivity.this,
                        FocusTimerActivity.class);

                startActivity(intent);

                return true;
            }

            Toast.makeText(HomeActivity.this, item.getTitle() + " Coming Soon!", Toast.LENGTH_SHORT).show();
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .commit();
    }
}