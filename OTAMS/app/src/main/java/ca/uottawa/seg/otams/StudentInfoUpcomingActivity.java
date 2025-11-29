package ca.uottawa.seg.otams;

import static ca.uottawa.seg.otams.ManageAvailabilityActivity.sdf;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentInfoUpcomingActivity extends AppCompatActivity {

    private String sessionId;
    private TextView tutorName;
    private TextView tutorEmail;
    private TextView tutorPhoneNumber;
    private TextView sessionCourse;
    private TextView sessionTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_info_pending);

        // Text fields that need to be populated with the session's info
        tutorName = findViewById(R.id.detail_full_name);
        tutorEmail = findViewById(R.id.detail_session_email);
        tutorPhoneNumber = findViewById(R.id.session_phone_number);
        sessionCourse = findViewById(R.id.detail_course);
        sessionTime = findViewById(R.id.detail_time);

        // Stores the session ID that was passed from the previous activity
        Intent intent = getIntent();
        sessionId = intent.getStringExtra("id");

        // Fetch the session details from the database
        fetchSessionDetails(sessionId);
    }

    private void fetchSessionDetails(String sessionId) {
        // Fetch session from database
        DatabaseReference session = FirebaseDatabase.getInstance().getReference("sessions").child(sessionId);
        session.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch & store session
                    Session s = snapshot.getValue(Session.class);

                    if (s != null) {
                        // Display tutor's name
                        tutorName.setText(s.getTutorName());

                        // Display session course
                        sessionCourse.setText(s.getCourses());

                        // Format and display the session time
                        String formattedTime = s.getDate() + " from " + sdf.format(s.getStartTime()) + " to " + sdf.format(s.getEndTime());
                        sessionTime.setText(formattedTime);

                        // Fetch the tutor's additional details (email and phone) from the users portion of the database
                        fetchTutorDetails(s.getTutorPhoneNumber());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void fetchTutorDetails(String phone) {
        // Fetch the tutor from the users portion of the database by using their phone number
        DatabaseReference tutor = FirebaseDatabase.getInstance().getReference("users").child(phone);
        tutor.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch and store the tutor's email and phone number
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phoneNumber").getValue(String.class);

                    // Display the tutor's contact information
                    tutorEmail.setText(email);
                    tutorPhoneNumber.setText(phone);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void onClickCancel(View view) {
        // Change the session back to an open timeslot
        if (sessionId != null) {
            DatabaseReference session = FirebaseDatabase.getInstance().getReference("sessions").child(sessionId);
            session.child("studentName").setValue(null);
            session.child("studentPhoneNumber").setValue(null);
            session.child("sessionStatus").setValue("OPEN");
        }
        finish(); // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
    }

    public void onClickBackToDashboard(View view) {
        int pressID = view.getId();

        // Check if the student is trying to return to their dashboard
        if (pressID == R.id.backToDashboardBtn) {
            // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
            finish();
        }
    }
}