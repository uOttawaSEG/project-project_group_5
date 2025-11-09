package ca.uottawa.seg.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ca.uottawa.seg.otams.email.EmailTask;

public class TutorRequestDetailsActivity extends AppCompatActivity {

    private TextView firstName;
    private TextView lastName;
    private TextView email;
    private TextView phoneNumber;
    private TextView program;
    private TextView coursesOffered;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        // Text fields that need to be populated with the specified request's info
        firstName = findViewById(R.id.detailFirstName);
        lastName = findViewById(R.id.detailLastName);
        email = findViewById(R.id.detailEmail);
        phoneNumber = findViewById(R.id.detailPhoneNumber);
        program = findViewById(R.id.detailProgramOrDegree);
        coursesOffered = findViewById(R.id.detailCoursesOffered);

        // Stores the info about the request that was passed from the previous activity
        Intent intent = getIntent();

        String requestFirstName = intent.getStringExtra("firstName");
        String requestLastName = intent.getStringExtra("lastName");
        String requestEmail = intent.getStringExtra("email");
        String requestPhoneNumber = intent.getStringExtra("phoneNumber");
        String requestProgram = intent.getStringExtra("program");
        String requestCoursesOffered = intent.getStringExtra("coursesOffered");

        // Changes the placeholder text to the info for the specified request
        firstName.setText(requestFirstName);
        lastName.setText(requestLastName);
        email.setText(requestEmail);
        phoneNumber.setText(requestPhoneNumber);
        program.setText(requestProgram);
        coursesOffered.setText(requestCoursesOffered);
    }

    public void onClickApprove(View view) {

    }

    public void onClickReject(View view) {

    }

    public void onClickCancel(View view){

    }

    public void onClickBackToDashboard(View view) {
        int pressID=view.getId();

        // Check if the administrator is trying to return back to their dashboard
        if (pressID == R.id.backToDashboardBtn) {
            /*
            // Set the next page as the dashboard
            Intent intent = new Intent(AdminRequestDetailsActivity.this, AdminDashboardActivity.class);

            // Send the administrator back to the dashboard
            startActivity(intent);
            */

            finish(); // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
        }
    }
}
