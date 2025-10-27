package ca.uottawa.seg.otams;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminRequestDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        // Text field that need to be populated with request info
        TextView firstName = (TextView) findViewById(R.id.detailFirstName);
        TextView lastName = (TextView) findViewById(R.id.detailLastName);
        TextView email = (TextView) findViewById(R.id.detailEmail);
        TextView role = (TextView) findViewById(R.id.detailRole);
        TextView phoneNumber = (TextView) findViewById(R.id.detailPhoneNumber);
        TextView programOrDegree = (TextView) findViewById(R.id.detailProgramOrDegree);
        TextView coursesOffered = (TextView) findViewById(R.id.detailCoursesOffered);

        // Stores the info about the user that was passed from the previous activity
        Intent intent = getIntent();

        String requestFirstName = intent.getStringExtra("firstName");
        String requestLastName = intent.getStringExtra("lastName");
        String requestEmail = intent.getStringExtra("email");
        String requestRole = intent.getStringExtra("role");
        String requestPhoneNumber = intent.getStringExtra("phoneNumber");

        // Different field are populated depending on whether the request is from a student or tutor
        if (requestRole.equals("Student")) {
            String requestProgram = intent.getStringExtra("program");

            programOrDegree.setText("Program: " + requestProgram);
            coursesOffered.setText(""); // Set courses offered field to blank since a student does not fill this field out during registration
        }
        else if (requestRole.equals("Tutor")) {
            String requestDegree = intent.getStringExtra("highestDegree");
            String requestCoursesOffered = intent.getStringExtra("coursesOffered");

            programOrDegree.setText("Highest degree: " + requestDegree);
            coursesOffered.setText("Courses offered: " + requestCoursesOffered);
        }

        // Changes the placeholder text to the info for the specified request
        firstName.setText(requestFirstName);
        lastName.setText(requestLastName);
        email.setText(requestEmail);
        role.setText(requestRole);
        phoneNumber.setText(requestPhoneNumber);
    }

    public void onClickApprove(View view) {
        int pressID=view.getId();

        // Check if the administrator approved the request
        if (pressID == R.id.approveButton) {
            setRequestStatus(RegistrationStatus.APPROVED); // Set the status of the request to approved if they did

            finish(); // Remove the current activity from the activity stack (go back to the previous activity)
        }
    }

    public void onClickReject(View view) {
        int pressID=view.getId();

        // Check if the administrator rejected the request
        if (pressID == R.id.rejectButton) {
            setRequestStatus(RegistrationStatus.REJECTED); // Set the status of the request to rejected if they did

            finish(); // Remove the current activity from the activity stack (go back to the previous activity)
        }
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

            finish(); // Remove the current activity from the activity stack (go back to the previous activity)
        }
    }

    public void setRequestStatus(RegistrationStatus requestStatus) {
        // Grab all users info from database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        // Determine the id (phone number) of the user entry the request belonged to
        TextView userIdTextView = findViewById(R.id.detailPhoneNumber);
        String userId = userIdTextView.getText().toString();
        // String userId = ((TextView) findViewById(R.id.detailPhone)).toString();

        // Find the user in the database that had its request status changed (use the phone number since that is the id for every entry in the database)
        reference.child(userId).child("requestStatus").setValue(requestStatus.toString());
    }
}