package ca.uottawa.seg.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.SearchView;

public class CourseSearchActivity extends AppCompatActivity {

    private String studentEmail;
    private String studentPhoneNumber;
    private SearchView course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_search);

        // Stores the email of the student that logged in that was passed from the previous activity
        Intent intent = getIntent();
        studentEmail = intent.getStringExtra("email");
        studentPhoneNumber = intent.getStringExtra("phoneNumber");

        course = findViewById(R.id.courseSearchBar);
    }

    public void onClickBackSearch(View view) {
        int pressID=view.getId();

        // Check if the student is trying to search for courses
        if (pressID == R.id.button_search) {
            // Get the text typed in the SearchView
            String enteredCourseCode = course.getQuery().toString().trim();

            // Make sure that the student actually typed something in
            if (enteredCourseCode.isEmpty()) {
                course.setQueryHint("Please enter a course code");
                return;
            }

            // Pass the course they are looking for sessions for and the student's phone number to the next page
            Intent intent = new Intent(CourseSearchActivity.this, MainActivity.class);
            intent.putExtra("phoneNumber", studentPhoneNumber);
            intent.putExtra("courseCode", enteredCourseCode);
            startActivity(intent);
        }
    }

    //return to dashboard button
    public void onClickBackToDashboard(View view) {
        int pressID=view.getId();

        // Check if the student is trying to return to their dashboard
        if (pressID == R.id.button_backToStudentDashboard) {
            // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
            finish();
        }
    }

}
