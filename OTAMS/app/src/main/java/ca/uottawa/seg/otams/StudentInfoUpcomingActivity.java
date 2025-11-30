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

public class StudentInfoUpcomingActivity extends AppCompatActivity {

    private String sessionId;
    private TextView tutorName;
    private TextView tutorEmail;
    private TextView tutorPhoneNumber;
    private TextView sessionCourse;
    private TextView sessionTime;
    private Session currentSession; // Store session for cancellation check

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
                        // Reconstruct the start and end times from Firebase
                        Date startTime = reconstructSessionDate(snapshot.child("startTime"));
                        Date endTime = reconstructSessionDate(snapshot.child("endTime"));
                        s.setStartTime(startTime);
                        s.setEndTime(endTime);

                        currentSession = s; // Store for later use

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

    // Properly reconstructs the start and end time of a session fetched from the database as a date object
    private Date reconstructSessionDate(DataSnapshot snapshot) {
        if (snapshot.exists()) {
            // Fetch the values of each part of the start or end time in the database
            Integer date = snapshot.child("date").getValue(Integer.class);
            Integer year = snapshot.child("year").getValue(Integer.class);
            Integer month = snapshot.child("month").getValue(Integer.class);
            Integer hours = snapshot.child("hours").getValue(Integer.class);
            Integer minutes = snapshot.child("minutes").getValue(Integer.class);
            Integer seconds = snapshot.child("seconds").getValue(Integer.class);

            // Check that the date related values are valid before setting anything
            if (year == null || month == null || date == null) {
                return null;
            }

            // If they are valid then create an object that represents the exact start/end time
            Calendar cal = Calendar.getInstance();

            // Set date related values
            cal.set(Calendar.DATE, date);
            cal.set(Calendar.YEAR, year + 1900);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, date);

            // Set time related values
            if (hours != null) {
                cal.set(Calendar.HOUR_OF_DAY, hours);
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 0);
            }
            if (minutes != null) {
                cal.set(Calendar.MINUTE, minutes);
            } else {
                cal.set(Calendar.MINUTE, 0);
            }
            if (seconds != null) {
                cal.set(Calendar.SECOND, seconds);
            } else {
                cal.set(Calendar.SECOND, 0);
            }
            cal.set(Calendar.MILLISECOND, 0);

            return cal.getTime();
        }
        return null;
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
        // Check whether session is >24 hours from current time
        if (sessionId != null && currentSession != null && isMoreThan24HoursAway(currentSession)) {
            DatabaseReference session = FirebaseDatabase.getInstance().getReference("sessions").child(sessionId);
            session.child("studentName").setValue(null);
            session.child("studentPhoneNumber").setValue(null);
            session.child("sessionStatus").setValue("OPEN");

            Toast.makeText(this, "Session cancelled successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Remove the current activity from the activity stack
        } else {
            // Show error message if cancellation is not allowed
            Toast.makeText(this, "ERROR: Sessions less than 24 hours away cannot be cancelled", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isMoreThan24HoursAway(Session session) {
        try {
            // Get current time
            Date currentTime = new Date();

            // Get session start time
            Date sessionStartTime = session.getStartTime();

            // Make sure session start time is not null
            if (sessionStartTime == null) {
                return false;
            }

            // Calculate the difference in milliseconds
            long diffInMillis = sessionStartTime.getTime() - currentTime.getTime();

            // Convert to hours
            long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);

            // Return true if more than 24 hours away
            return diffInHours > 24;

        } catch (Exception e) {
            // If there's an error, default to not allowing cancellation
            return false;
        }
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