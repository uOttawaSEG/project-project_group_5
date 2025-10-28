package ca.uottawa.seg.otams;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeAdminPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_welcome);
    }
    public void onClickDashboard(View view) {
        int pressID=view.getId();

        // Check if the user is trying to access the admin dashboard
        if (pressID == R.id.button_dashboard) {
            // Set the next page to the admin dashboard page
            Intent intent = new Intent(WelcomeAdminPageActivity.this, AdminDashboardActivity.class);

            // Send the user to the admin dashboard page
            startActivity(intent);
        }
    }

    public void onClickLogOff(View view) {
        int pressID=view.getId();

        // Check if the user is trying to log out
        if (pressID == R.id.button_log_off) {
            // Set the next page to the login page
            Intent intent = new Intent(WelcomeAdminPageActivity.this, MainActivity.class);

            // Send the user to the login page
            startActivity(intent);
        }
    }
}
