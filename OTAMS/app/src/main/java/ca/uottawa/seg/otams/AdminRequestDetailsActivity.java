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

public class AdminRequestDetailsActivity extends AppCompatActivity {

    private TextView firstName;
    private TextView lastName;
    private TextView email;
    private TextView role;
    private TextView phoneNumber;
    private TextView programOrDegree;
    private TextView coursesOffered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        // Text fields that need to be populated with the specified request's info
        firstName = findViewById(R.id.detail_name);
        lastName = findViewById(R.id.detailLastName);
        email = findViewById(R.id.detail_email);
        role = findViewById(R.id.detailRole);
        phoneNumber = findViewById(R.id.detail_phone_number);
        programOrDegree = findViewById(R.id.session_program);
        coursesOffered = findViewById(R.id.detailCoursesOffered);

        // Stores the info about the request that was passed from the previous activity
        Intent intent = getIntent();

        String requestFirstName = intent.getStringExtra("firstName");
        String requestLastName = intent.getStringExtra("lastName");
        String requestEmail = intent.getStringExtra("email");
        String requestRole = intent.getStringExtra("role");
        String requestPhoneNumber = intent.getStringExtra("phoneNumber");

        // Different fields are populated depending on whether the request is from a student or tutor
        if ("Student".equals(requestRole)) {
            String requestProgram = intent.getStringExtra("program");

            programOrDegree.setText("Program: " + requestProgram);
            coursesOffered.setText(""); // Set courses offered field to blank since a student does not fill this field out during registration
        }
        else if ("Tutor".equals(requestRole)) {
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

    private void sendEmail(RegistrationStatus rs) {
        EmailTask.Builder builder = EmailTask.builder();
        if (rs == RegistrationStatus.APPROVED) {
            builder.setBody("Your registration request has been approved");
            builder.setSubject("APPROVED!");
        }
        if (rs == RegistrationStatus.REJECTED) {
            builder.setBody("Your registration request has been rejected");
            builder.setSubject("REJECTED!");
        }
        builder.setTo(email.getText().toString());
        builder.setCallback(new EmailTask.Callback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(AdminRequestDetailsActivity.this,
                            "Email sent successfully!", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(AdminRequestDetailsActivity.this,
                            "Failed to send email: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
        builder.build().send();
    }

    public void onClickApprove(View view) {
        int pressID=view.getId();
        // Check if the administrator approved the request
        if (pressID == R.id.approveButton) {
            setRequestStatus(RegistrationStatus.APPROVED); // Set the status of the request to approved if they did
            sendEmail(RegistrationStatus.APPROVED);
            // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
            finish();
        }
    }

    public void onClickReject(View view) {
        int pressID=view.getId();

        // Check if the administrator rejected the request
        if (pressID == R.id.rejectButton) {
            setRequestStatus(RegistrationStatus.REJECTED); // Set the status of the request to rejected if they did
            sendEmail(RegistrationStatus.REJECTED);
            finish(); // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
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

            finish(); // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
        }
    }

    public void setRequestStatus(RegistrationStatus requestStatus) {
        // Grab all users' info from the database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        // Determine the id (phone number) of the user entry the request was from
        TextView userIdTextView = findViewById(R.id.detail_phone_number);
        String userId = userIdTextView.getText().toString();

        // Find the user in the database that had its request status changed (use the phone number since that is the id for every entry in the database) and update its status accordingly
        reference.child(userId).child("requestStatus").setValue(requestStatus.toString());
    }
}