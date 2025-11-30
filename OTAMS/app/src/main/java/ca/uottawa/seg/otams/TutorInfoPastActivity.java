package ca.uottawa.seg.otams;

import static ca.uottawa.seg.otams.ManageAvailabilityActivity.sdf;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TutorInfoPastActivity extends AppCompatActivity {

    private String sessionId;
    private TextView studentName;
    private TextView studentEmail;
    private TextView studentPhoneNumber;
    private TextView sessionCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_info_past);

        // Text fields that need to be populated with the session's info
        studentName = findViewById(R.id.detail_full_name);
        studentEmail = findViewById(R.id.detail_session_email);
        studentPhoneNumber = findViewById(R.id.session_phone_number);
        sessionCourse = findViewById(R.id.detail_course);

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
                        // Display student's name
                        studentName.setText(s.getStudentName());

                        // Display session course
                        sessionCourse.setText(s.getCourses());

                        // Fetch the student's additional details (email and phone) from the users portion of the database
                        fetchStudentDetails(s.getStudentPhoneNumber());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void fetchStudentDetails(String phone) {
        // Fetch the student from the users portion of the database by using their phone number
        DatabaseReference student = FirebaseDatabase.getInstance().getReference("users").child(phone);
        student.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch and store the student's email and phone number
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phoneNumber").getValue(String.class);

                    // Display the student's contact information
                    studentEmail.setText(email);
                    studentPhoneNumber.setText(phone);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void onClickBackToDashboard(View view) {
        int pressID = view.getId();

        // Check if the tutor is trying to return to their dashboard
        if (pressID == R.id.backToDashboardBtn) {
            // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
            finish();
        }
    }
}