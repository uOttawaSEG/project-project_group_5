package ca.uottawa.seg.otams;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class RegistrationPendingPageActivity extends Activity {
    public void onClickLogOff(View view) {
        int pressID=view.getId();

        // Check if the user is trying to log out
        if (pressID == R.id.button_log_off) {
            // Set the next page to the login page
            Intent intent = new Intent(RegistrationPendingPageActivity.this, MainActivity.class);

            // Send the user to the login page
            startActivity(intent);
        }
    }
}
