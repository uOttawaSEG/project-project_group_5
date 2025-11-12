package ca.uottawa.seg.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentInformationActivity extends AppCompatActivity {

    public static final String STUDENT_NAME = "studentName";
    public static final String PHONE_NUMBER = "phoneNumber";

    private TextView name;
    private TextView phoneNumber;
    private TextView email;
    private TextView program;

    private String sessionId;
    private String studentPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_information);

        name = findViewById(R.id.detail_full_name);
        phoneNumber = findViewById(R.id.session_phone_number);
        email = findViewById(R.id.detailEmailText);     // New TextView for email
        program = findViewById(R.id.session_program); // New TextView for program

        Intent intent = getIntent();
        name.setText(intent.getStringExtra(STUDENT_NAME));
        studentPhone = intent.getStringExtra(PHONE_NUMBER);
        phoneNumber.setText(studentPhone);

        sessionId = intent.getStringExtra("id"); // Session ID

        fetchStudentDetails(studentPhone);
    }

    /**
     * Fetch email and program from "users" node using phone number
     */
    private void fetchStudentDetails(String phone) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(phone);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String studentEmail = snapshot.child("email").getValue(String.class);
                String studentProgram = snapshot.child("program").getValue(String.class);

                if (studentEmail != null) email.setText(studentEmail);
                if (studentProgram != null) program.setText(studentProgram);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(StudentInformationActivity.this, "Failed to fetch student details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClickApprove(View view) {
        if (sessionId == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("sessions")
                .child(sessionId);
        ref.child("sessionStatus").setValue(RegistrationStatus.APPROVED.toString())
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Session approved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to approve: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        finish();
    }

    public void onClickReject(View view) {
        if (sessionId == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("sessions")
                .child(sessionId);
        ref.removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Session rejected", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to reject: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        finish();
    }

    public void onClickBackToDashboard(View view) {
        finish();
    }
}
