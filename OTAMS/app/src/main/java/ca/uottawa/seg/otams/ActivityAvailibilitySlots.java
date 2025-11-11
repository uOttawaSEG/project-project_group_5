package ca.uottawa.seg.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityAvailibilitySlots extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_availibility_slots);


    }

    public void onClickBackToDashboard(View view) {
        int pressID=view.getId();

        // Check if the user is trying to log out
        if (pressID == R.id.button_log_off) {
            // Set the next page to the login page
            Intent intent = new Intent(ActivityAvailibilitySlots.this, TutorDashboardActivity.class);

            // Send the user to the login page
            startActivity(intent);
        }
    }
}
