package ca.uottawa.seg.otams;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeStudentPageActivity extends AppCompatActivity {

    private String studentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_welcome);

        // Stores the phone number of the tutor that logged in that was passed from the previous activity
        Intent intent = getIntent();
        studentEmail = intent.getStringExtra("email");
    }

    public void onClickSearchCourse(View view) {
        int pressID=view.getId();

        if (pressID == R.id.button_search) {
            // Set the next page to the search course page
            Intent intent = new Intent(WelcomeStudentPageActivity.this, CourseSearchActivity.class);

            // Pass the student's email to the next page so that the student can quickly be identified and found in the database (since the email is the key)
            intent.putExtra("email", studentEmail);

            // Send the user to the course search page
            startActivity(intent);
        }
    }

    public void onClickDashboard(View view) {
        int pressID=view.getId();

        // Check if the user is trying to access the admin dashboard
        if (pressID == R.id.button_dashboard) {
            // Set the next page to the student dashboard page
            Intent intent = new Intent(WelcomeStudentPageActivity.this, StudentDashboardActivity.class);

            // Pass the student's phone number to the next page so that the student can quickly be identified and found in the database (since the phone number is the key)
            intent.putExtra("email", studentEmail);

            // Send the user to the student dashboard page
            startActivity(intent);
        }
    }

    public void onClickLogOff(View view) {
        int pressID=view.getId();

        // Check if the user is trying to log out
        if (pressID == R.id.button_log_off) {
            // Set the next page to the login page
            Intent intent = new Intent(WelcomeStudentPageActivity.this, MainActivity.class);

            // Send the user to the login page
            startActivity(intent);
        }
    }


}
