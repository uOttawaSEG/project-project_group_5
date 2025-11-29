package ca.uottawa.seg.otams;

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

public class StudentInfoPastActivity extends AppCompatActivity {

    public static final String PHONE_NUMBER = "phoneNumber";
    private String sessionId;
    private String studentPhoneNumber;
    private TextView name;
    private TextView email;
    private TextView phoneNumber;
    private TextView course;

    static final String TUTOR_NAME = "tutorName";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_info_past);

        // Text fields that need to be populated with the specified tutor's info
        name = findViewById(R.id.detail_full_name);
        email = findViewById(R.id.detail_session_email);
        phoneNumber = findViewById(R.id.session_phone_number);
        course = findViewById(R.id.detail_course);

        // Stores the info about the session that was passed from the previous activity
        Intent intent = getIntent();
        sessionId = intent.getStringExtra("id");
        studentPhoneNumber = intent.getStringExtra("studentPhoneNumber");

        // Fetch session and tutor details
        fetchSessionDetails(sessionId);
    }

    private void fetchSessionDetails(String sessionId) {
        DatabaseReference session = FirebaseDatabase.getInstance().getReference("sessions").child(sessionId);
        session.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Session s = snapshot.getValue(Session.class);

                    if (s != null) {
                        // Display tutor name
                        name.setText(s.getTutorName());

                        // Display course
                        course.setText(s.getCourses());

                        // Fetch tutor's contact details
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
                    // Fetch and store the tutor's email
                    String tutorEmail = snapshot.child("email").getValue(String.class);

                    // Changes the placeholder text to the info for the specified tutor
                    email.setText(tutorEmail);
                    phoneNumber.setText(phone);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void onClickBackToDashboard(View view) {
        int pressID=view.getId();

        // Check if the student is trying to return to their dashboard
        if (pressID == R.id.backToDashboardBtn) {
            // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
            finish();
        }
    }

    public void onClickRateTutor(View view) {
        int pressID=view.getId();

        // Check if the student wants to rate the tutor
        if (pressID == R.id.rateTutorBtn) {
            Intent intent = new Intent(StudentInfoPastActivity.this, RateTutorActivity.class);

            // Pass the session ID and student phone number to the rating activity
            intent.putExtra("sessionId", sessionId);
            intent.putExtra("studentPhoneNumber", studentPhoneNumber);

            // Send the user to the rating page
            startActivity(intent);
        }
    }
}