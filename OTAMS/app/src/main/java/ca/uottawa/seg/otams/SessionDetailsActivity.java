package ca.uottawa.seg.otams;


import static android.content.Intent.getIntent;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SessionDetailsActivity extends AppCompatActivity {

    private TextView firstName;
    private TextView lastName;
    private TextView email;
    private TextView phoneNumber;
    private TextView program;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);

        // Text fields that need to be populated with the specified request's info
        firstName = findViewById(R.id.detailFirstName);
        lastName = findViewById(R.id.detailLastName);
        email = findViewById(R.id.detailEmail);
        phoneNumber = findViewById(R.id.detailPhoneNumber);
        program = findViewById(R.id.detailProgramOrDegree);

        // Stores the info about the request that was passed from the previous activity
        Intent intent = getIntent();

        String requestFirstName = intent.getStringExtra("firstName");
        String requestLastName = intent.getStringExtra("lastName");
        String requestEmail = intent.getStringExtra("email");
        String requestPhoneNumber = intent.getStringExtra("phoneNumber");
        String requestProgram = intent.getStringExtra("program");

        // Changes the placeholder text to the info for the specified request
        firstName.setText(requestFirstName);
        lastName.setText(requestLastName);
        email.setText(requestEmail);
        phoneNumber.setText(requestPhoneNumber);
        program.setText(requestProgram);
    }

    //METHOD IS INCOMPLETE
    public void onClickReject(View view) {
        int pressID=view.getId();

        // Check if the administrator rejected the request
        if (pressID == R.id.rejectButton) {
            setSessionStatus("REJECTED");

            finish(); // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
        }
    }

    //METHOD IS INCOMPLETE
    public void onClickApprove(View view) {
        int pressID=view.getId();
        // Check if the administrator approved the request
        if (pressID == R.id.approveButton) {
            setSessionStatus("APPROVED");
            // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
            finish();
        }
    }

    private void setSessionStatus(String updatedStatus) {
        Intent intent = getIntent();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("sessions");
        ref.child(intent.getStringExtra("id")).child("sessionStatus").setValue(updatedStatus);
    }


    public void onClickBackToDashboard(View view) {
        int pressID=view.getId();

        // Check if the user is trying to access the admin dashboard
        if (pressID == R.id.button_dashboard) {
            // Set the next page to the admin dashboard page
            Intent intent = new Intent(SessionDetailsActivity.this, TutorDashboardActivity.class);


            // Send the user to the tutor dashboard page
            startActivity(intent);
        }
    }

    //copied from AdminRequestDetailsAcitivity.java
    //applies to users not sessions
    //i want to search by session id but im so confused and dont know how (╥﹏╥)
    /*
    public void setRequestStatus(RegistrationStatus requestStatus) {
        // Grab all users' info from the database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        // Determine the id (phone number) of the user entry the request was from
        TextView userIdTextView = findViewById(R.id.detailPhoneNumber);
        String userId = userIdTextView.getText().toString();

        // Find the user in the database that had its request status changed (use the phone number since that is the id for every entry in the database) and update its status accordingly
        reference.child(userId).child("requestStatus").setValue(requestStatus.toString());
    }
     */
}


