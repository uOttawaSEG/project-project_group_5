package ca.uottawa.seg.otams;

import static ca.uottawa.seg.otams.ManageAvailabilityActivity.sdf;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RateTutorActivity extends AppCompatActivity {

    private String sessionId;
    private String tutorPhoneNumber;
    private String studentPhoneNumber;

    private TextView tutorName;
    private TextView tutorCourse;
    private TextView sessionTime;
    private RatingBar ratingBar;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_your_tutor);

        // Initialize views
        tutorName = findViewById(R.id.tutorName);
        tutorCourse = findViewById(R.id.tutorCourse);
        sessionTime = findViewById(R.id.sessionTime);
        ratingBar = findViewById(R.id.ratingBar);
        submitButton = findViewById(R.id.submitRatingButton);

        // Get data from intent
        Intent intent = getIntent();
        sessionId = intent.getStringExtra("sessionId");
        studentPhoneNumber = intent.getStringExtra("studentPhoneNumber");

        // Fetch session details
        fetchSessionDetails(sessionId);

        // Set up submit button
        submitButton.setOnClickListener(v -> submitRating());
    }

    private void fetchSessionDetails(String sessionId) {
        DatabaseReference session = FirebaseDatabase.getInstance().getReference("sessions").child(sessionId);
        session.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Session s = snapshot.getValue(Session.class);

                    if (s != null) {
                        tutorPhoneNumber = s.getTutorPhoneNumber();

                        // Display tutor name
                        tutorName.setText(s.getTutorName());

                        // Display course
                        tutorCourse.setText("Course: " + s.getCourses());

                        // Display date and time
                        String timeText = s.getDate() + " from " + sdf.format(s.getStartTime()) + " to " + sdf.format(s.getEndTime());
                        sessionTime.setText(timeText);

                        // Check if this session has already been rated
                        checkIfAlreadyRated();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RateTutorActivity.this, "Failed to load session details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfAlreadyRated() {
        DatabaseReference ratings = FirebaseDatabase.getInstance().getReference("ratings");
        Query checkRating = ratings.orderByChild("sessionId").equalTo(sessionId);

        checkRating.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Session has already been rated
                    for (DataSnapshot ratingSnapshot : snapshot.getChildren()) {
                        Rating existingRating = ratingSnapshot.getValue(Rating.class);
                        if (existingRating != null && existingRating.getStudentPhoneNumber().equals(studentPhoneNumber)) {
                            // Display existing rating and disable submission
                            ratingBar.setRating(existingRating.getRatingValue());
                            ratingBar.setIsIndicator(true);
                            submitButton.setEnabled(false);
                            submitButton.setText("Already Rated");
                            Toast.makeText(RateTutorActivity.this, "You have already rated this session", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void submitRating() {
        float ratingValue = ratingBar.getRating();

        if (ratingValue == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent multiple submissions
        submitButton.setEnabled(false);

        DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("ratings");
        String ratingId = ratingsRef.push().getKey();

        Rating newRating = new Rating(
                ratingId,
                tutorPhoneNumber,
                studentPhoneNumber,
                sessionId,
                (int) ratingValue,
                System.currentTimeMillis()
        );

        // Save the rating
        ratingsRef.child(ratingId).setValue(newRating).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update tutor's average rating
                updateTutorAverageRating();
            } else {
                Toast.makeText(RateTutorActivity.this, "Failed to submit rating", Toast.LENGTH_SHORT).show();
                submitButton.setEnabled(true);
            }
        });
    }

    private void updateTutorAverageRating() {
        DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("ratings");
        Query tutorRatings = ratingsRef.orderByChild("tutorPhoneNumber").equalTo(tutorPhoneNumber);

        tutorRatings.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalRatings = 0;
                float sumRatings = 0;

                for (DataSnapshot ratingSnapshot : snapshot.getChildren()) {
                    Rating rating = ratingSnapshot.getValue(Rating.class);
                    if (rating != null) {
                        sumRatings += rating.getRatingValue();
                        totalRatings++;
                    }
                }

                if (totalRatings > 0) {
                    float averageRating = sumRatings / totalRatings;

                    // Update tutor's average rating in the database
                    DatabaseReference tutorRef = FirebaseDatabase.getInstance().getReference("users").child(tutorPhoneNumber);
                    tutorRef.child("avgRatingValue").setValue(averageRating).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(RateTutorActivity.this, "Rating submitted successfully!", Toast.LENGTH_SHORT).show();
                            finish(); // Return to previous activity
                        } else {
                            Toast.makeText(RateTutorActivity.this, "Failed to update tutor rating", Toast.LENGTH_SHORT).show();
                            submitButton.setEnabled(true);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RateTutorActivity.this, "Failed to calculate average rating", Toast.LENGTH_SHORT).show();
                submitButton.setEnabled(true);
            }
        });
    }

    public void onClickBackArrow(View view) {
        finish(); // Return to previous activity
    }
}