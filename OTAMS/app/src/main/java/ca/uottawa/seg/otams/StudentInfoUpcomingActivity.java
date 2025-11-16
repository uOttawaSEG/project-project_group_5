package ca.uottawa.seg.otams;

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

import java.util.Objects;

public class StudentInfoUpcomingActivity extends AppCompatActivity {

    // public static final String EMAIL = "email";
    public static final String PHONE_NUMBER = "phoneNumber";
    // public static final String PROGRAM = "program";
    private String sessionId;
    private TextView name;
    private TextView email;
    private TextView phoneNumber;
    private TextView program;

    static final String STUDENT_NAME = "studentName";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_info_upcoming);

        // Text fields that need to be populated with the specified student's info
        name = findViewById(R.id.detail_full_name);
        email = findViewById(R.id.detail_session_email);
        phoneNumber = findViewById(R.id.session_phone_number);
        program = findViewById(R.id.detail_program);

        // Stores the info about the student that was passed from the previous activity
        Intent intent = getIntent();

        sessionId = intent.getStringExtra("id");

        // Changes the placeholder text to the info for the specified student
        name.setText(intent.getStringExtra(STUDENT_NAME));
        phoneNumber.setText(intent.getStringExtra(PHONE_NUMBER));
        // email.setText(intent.getStringExtra(EMAIL));
        // program.setText(intent.getStringExtra(PROGRAM));

        // Fetch the missing student details (i.e. their email and program) from the users portion of the database
        fetchStudentDetails(intent.getStringExtra(PHONE_NUMBER));
    }

    private void fetchStudentDetails(String phone) {
        // Fetch the student from the users portion of the database by using their phone number (i.e. the id for that student's entry)
        DatabaseReference student = FirebaseDatabase.getInstance().getReference("users").child(phone);
        student.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch and store the student's email and program
                    String studentEmail = snapshot.child("email").getValue(String.class);
                    String studentProgram = snapshot.child("program").getValue(String.class);

                    // Changes the placeholder text to the info for the specified student
                    email.setText(studentEmail);
                    program.setText(studentProgram);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void onClickCancel(View view) {
        // Change the session to an open timeslot
        if (sessionId != null) {
            DatabaseReference session = FirebaseDatabase.getInstance().getReference("sessions").child(sessionId);
            session.child("studentName").setValue(null);
            session.child("studentPhoneNumber").setValue(null);
            session.child("sessionStatus").setValue("OPEN");
        }
        finish(); // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
    }

    public void onClickBackToDashboard(View view) {
        int pressID=view.getId();

        // Check if the tutor is trying to return to their dashboard
        if (pressID == R.id.backToDashboardBtn) {

            // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
            finish();
        }
    }
}


