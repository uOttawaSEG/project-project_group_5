package ca.uottawa.seg.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class StudentInformationActivity extends AppCompatActivity {

    public static final String STUDENT_NAME = "studentName";
    public static final String PHONE_NUMBER = "phoneNumber";

    private TextView name;
    private TextView phoneNumber;

    private String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_information);

        name = findViewById(R.id.detail_full_name);
        phoneNumber = findViewById(R.id.session_phone_number);

        Intent intent = getIntent();
        name.setText(intent.getStringExtra(STUDENT_NAME));
        phoneNumber.setText(intent.getStringExtra(PHONE_NUMBER));

        sessionId = intent.getStringExtra("id"); // Session ID
    }

    public void onClickApprove(View view) {
        if (sessionId == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("sessions")
                .child(sessionId);
        ref.child("sessionStatus").setValue(RegistrationStatus.APPROVED.toString())
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Session approved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to approve: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        finish(); // Return to dashboard
    }

    public void onClickReject(View view) {
        if (sessionId == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("sessions")
                .child(sessionId);
        ref.removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Session rejected", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to reject: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        finish(); // Return to dashboard
    }

    public void onClickBackToDashboard(View view) {
        finish(); // Just go back
    }
}
