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

import java.util.Objects;

public class OpenTimeslotPageActivity extends AppCompatActivity {
    private String sessionId;
    private TextView date;
    private TextView time;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_timeslot_page);

        // Text fields that need to be populated with the specified timeslot's details
        date = findViewById(R.id.detail_date);
        time = findViewById(R.id.detail_time);

        // Stores the info about the timeslot that was passed from the previous activity
        Intent intent = getIntent();

        sessionId = intent.getStringExtra("id");

        // Fetch the missing session details
        fetchSessionDetails(sessionId);
    }

    private void fetchSessionDetails(String sessionId) {
        // Fetch the session from the database
        DatabaseReference session = FirebaseDatabase.getInstance().getReference("sessions").child(sessionId);
        session.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch and store the session
                    Session s = snapshot.getValue(Session.class);

                    if (s != null) {
                        // Fetch and format the time of the session for displaying purposes
                        String sessionTime = sdf.format(s.getStartTime()) + " - " + sdf.format(s.getEndTime());

                        // Changes the placeholder text to the details for the specified session
                        date.setText(s.getDate());
                        time.setText(sessionTime);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void onClickDelete(View view) {
        // Deletes the timeslot permanently (by removing it entirely from the database)
        if (sessionId != null) {
            DatabaseReference session = FirebaseDatabase.getInstance().getReference("sessions").child(sessionId);
            session.removeValue();
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