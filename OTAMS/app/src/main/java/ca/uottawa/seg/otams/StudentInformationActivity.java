package ca.uottawa.seg.otams;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class StudentInformationActivity extends AppCompatActivity {

    public static final String EMAIL = "email";
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final String PROGRAM = "program";
    private TextView name;
    private TextView email;
    private TextView phoneNumber;
    private TextView program;

    private String tutorPhoneNumber; // Stores the phone number of the tutor so their entry in the database can quickly be found (since phone number is the key)
    private String tutorEmail;
    static final String STUDENT_NAME = "studentName";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_information);

        // Text fields that need to be populated with the specified request's info
        name = findViewById(R.id.detail_full_name);
        email = findViewById(R.id.detail_session_email);
        phoneNumber = findViewById(R.id.session_phone_number);
        program = findViewById(R.id.session_program);

        // Stores the info about the request that was passed from the previous activity
        Intent intent = getIntent();
        // Changes the placeholder text to the info for the specified request
        name.setText(intent.getStringExtra(STUDENT_NAME));
        phoneNumber.setText(intent.getStringExtra(PHONE_NUMBER));

        tutorPhoneNumber = intent.getStringExtra("phoneNumber");
        tutorEmail = intent.getStringExtra("email");
    }

    //METHOD IS INCOMPLETE
    public void onClickReject(View view) {
        int pressID=view.getId();

        // Check if the administrator rejected the request
        if (pressID == R.id.rejectButton) {
            setSessionStatus(RegistrationStatus.REJECTED);
            Intent intent=getIntent();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("sessions");
            String sessionId = intent.getStringExtra("id");
            if(sessionId!=null){
                ref.child(sessionId).removeValue();//remove the session from the database
            }
            finish(); // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
        }
    }

    //METHOD IS INCOMPLETE
    public void onClickApprove(View view) {
        int pressID=view.getId();
        // Check if the administrator approved the request
        if (pressID == R.id.approveButton) {
            setSessionStatus(RegistrationStatus.APPROVED);
            // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
            finish();
        }
    }

    private void setSessionStatus(RegistrationStatus newStatus) {
        Intent intent = getIntent();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("sessions");
        ref.child(intent.getStringExtra("id")).child("sessionStatus").setValue(newStatus.toString());
    }


    public void onClickDashboard(View view) {
        int pressID=view.getId();

        // Check if the user is trying to access the admin dashboard
        if (pressID == R.id.backToDashboardBtn) {
            // Set the next page to the admin dashboard page
            Intent intent = new Intent(StudentInformationActivity.this, TutorDashboardActivity.class);

            // Pass the tutor's phone number to the next page so that the tutor can quickly be identified and found in the database (since the phone number is the key)
            intent.putExtra("phoneNumber", tutorPhoneNumber);
            intent.putExtra("email", tutorEmail);

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


