package ca.uottawa.seg.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

public class TutorDashboardActivity extends AppCompatActivity{

    //private RegistrationStatus rs = null;
    private RecyclerView recycleView;
    private UserListAdapter ula;
    String tutorPhoneNumber; // Stores the phone number of the tutor so their entry in the database can quickly be found (since phone number is the key)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_tutor_dashboard);

        // Stores the phone number of the tutor that was passed from the previous activity
        Intent intent = getIntent();
        tutorPhoneNumber = intent.getStringExtra("phoneNumber");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.back_button), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onClickManageAvailability(View view) {

        int pressID=view.getId();

        // Check if the user is trying to access the admin dashboard
        if (pressID == R.id.button_manage_availability) {
            // Set the next page to the admin dashboard page
            Intent intent = new Intent(TutorDashboardActivity.this, ManageAvailabilityActivity.class);

            // Pass the tutor's phone number to the next page so that the tutor can quickly be identified and found in the database (since the phone number is the key)
            intent.putExtra("phoneNumber", tutorPhoneNumber);

            // Send the user to the manage availability page (i.e. the one with the calender)
            startActivity(intent);
        }

    }

    public void onClickLogOff(View view) {
        int pressID=view.getId();

        // Check if the user is trying to log out
        if (pressID == R.id.button_log_off) {
            // Set the next page to the login page
            Intent intent = new Intent(TutorDashboardActivity.this, MainActivity.class);

            // Send the user to the login page
            startActivity(intent);
        }
    }

}
