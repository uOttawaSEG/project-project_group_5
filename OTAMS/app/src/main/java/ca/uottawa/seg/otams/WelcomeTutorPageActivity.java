package ca.uottawa.seg.otams;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeTutorPageActivity extends AppCompatActivity {

    private String tutorPhoneNumber; // Stores the phone number of the tutor so their entry in the database can quickly be found (since phone number is the key)
    private String tutorEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_welcome);

        // Stores the phone number of the tutor that logged in that was passed from the previous activity
        Intent intent = getIntent();
        tutorPhoneNumber = intent.getStringExtra("phoneNumber");
        tutorEmail = intent.getStringExtra("email");
    }
    public void onClickDashboard(View view) {
        int pressID=view.getId();

        // Check if the user is trying to access the admin dashboard
        if (pressID == R.id.button_dashboard) {
            // Set the next page to the admin dashboard page
            Intent intent = new Intent(WelcomeTutorPageActivity.this, TutorDashboardActivity.class);

            // Pass the tutor's phone number to the next page so that the tutor can quickly be identified and found in the database (since the phone number is the key)
            intent.putExtra("phoneNumber", tutorPhoneNumber);
            intent.putExtra("email", tutorEmail);

            // Send the user to the tutor dashboard page
            startActivity(intent);
        }
    }

    public void onClickLogOff(View view) {
        int pressID=view.getId();

        // Check if the user is trying to log out
        if (pressID == R.id.button_log_off) {
            // Set the next page to the login page
            Intent intent = new Intent(WelcomeTutorPageActivity.this, MainActivity.class);

            // Send the user to the login page
            startActivity(intent);
        }
    }


}
