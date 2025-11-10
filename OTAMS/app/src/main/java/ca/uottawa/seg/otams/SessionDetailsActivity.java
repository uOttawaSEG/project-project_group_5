package ca.uottawa.seg.otams;

import static android.content.Intent.getIntent;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SessionDetailsActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);

    }

    //METHOD IS INCOMPLETE
    public void onClickReject(View view) {
        int pressID=view.getId();

        // Check if the administrator rejected the request
        if (pressID == R.id.rejectButton) {

            finish(); // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
        }
    }

    //METHOD IS INCOMPLETE
    public void onClickApprove(View view) {
        int pressID=view.getId();
        // Check if the administrator approved the request
        if (pressID == R.id.approveButton) {
            // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
            finish();
        }
    }
    public void onClickDashboard(View view) {
        int pressID=view.getId();

        // Check if the user is trying to access the admin dashboard
        if (pressID == R.id.button_dashboard) {
            // Set the next page to the admin dashboard page
            Intent intent = new Intent(SessionDetailsActivity.this, TutorDashboardActivity.class);


            // Send the user to the tutor dashboard page
            startActivity(intent);
        }
    }
}


