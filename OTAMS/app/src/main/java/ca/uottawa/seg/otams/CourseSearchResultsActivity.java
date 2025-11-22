package ca.uottawa.seg.otams;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Loads sessions whose tutor offers the searched course.
 * Simple substring match on tutors' coursesOffered field (case-insensitive).
 */
public class CourseSearchResultsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private CourseResultsAdapter adapter;
    private List<CourseResult> results = new ArrayList<>();

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_search_results);

        recyclerView = findViewById(R.id.course_results_recycler_view);
        progressBar = findViewById(R.id.course_results_progress);
        emptyText = findViewById(R.id.course_results_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CourseResultsAdapter(results);
        recyclerView.setAdapter(adapter);

        String courseQuery = getIntent().getStringExtra("courseQuery");
        if (courseQuery == null || courseQuery.trim().isEmpty()) {
            Toast.makeText(this, "No course entered", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadMatchingSessions(courseQuery.trim());
    }

    private void loadMatchingSessions(String courseQuery) {
        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        results.clear();
        adapter.notifyDataSetChanged();

        DatabaseReference sessionsRef = FirebaseDatabase.getInstance().getReference("sessions");
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        sessionsRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot sessionsSnap) {
                if (!sessionsSnap.exists()) {
                    progressBar.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                    emptyText.setText("No sessions available.");
                    return;
                }

                // We'll process all sessions and keep track of how many processed for tutor lookups.
                final int totalSessions = (int) sessionsSnap.getChildrenCount();
                final int[] processedCount = {0};

                for (DataSnapshot sessionChild : sessionsSnap.getChildren()) {
                    // Read basic session fields
                    String sessionStatus = sessionChild.child("sessionStatus").getValue(String.class);
                    // Show only available/open slots (adjust as needed)
                    if (sessionStatus != null && !"OPEN".equalsIgnoreCase(sessionStatus)) {
                        processedCount[0]++;
                        if (processedCount[0] >= totalSessions) finishProcessing();
                        continue;
                    }

                    String tutorPhone = sessionChild.child("tutorPhoneNumber").getValue(String.class);
                    if (TextUtils.isEmpty(tutorPhone)) {
                        processedCount[0]++;
                        if (processedCount[0] >= totalSessions) finishProcessing();
                        continue;
                    }

                    // For each session, fetch tutor info
                    usersRef.child(tutorPhone).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot tutorSnap) {
                            try {
                                String coursesOffered = tutorSnap.child("coursesOffered").getValue(String.class);
                                String tutorName = tutorSnap.child("firstName").getValue(String.class);
                                String tutorLast = tutorSnap.child("lastName").getValue(String.class);
                                String tutorFullName = buildTutorName(tutorName, tutorLast);
                                Long ratingLong = null;
                                Integer ratingInt = null;
                                Object ratingObj = tutorSnap.child("rating").getValue();
                                if (ratingObj instanceof Long) ratingLong = (Long) ratingObj;
                                else if (ratingObj instanceof Integer) ratingInt = (Integer) ratingObj;
                                else if (ratingObj instanceof String) {
                                    try { ratingInt = Integer.parseInt((String) ratingObj); } catch (Exception ignored) {}
                                }

                                Double ratingDouble = null;
                                if (ratingLong != null) ratingDouble = ratingLong.doubleValue();
                                else if (ratingInt != null) ratingDouble = ratingInt.doubleValue();

                                // Check if the tutor offers the course (case-insensitive substring match)
                                boolean offers = false;
                                if (coursesOffered != null) {
                                    offers = coursesOffered.toLowerCase().contains(courseQuery.toLowerCase());
                                }

                                if (offers) {
                                    // Build session object info
                                    String sessionId = sessionChild.getKey();
                                    String dateString = sessionChild.child("date").getValue(String.class);

                                    // startTime may be stored as a timestamp (Long), or String. Try to handle both.
                                    Date startDate = null;
                                    Object startRaw = sessionChild.child("startTime").getValue();
                                    if (startRaw instanceof Long) startDate = new Date((Long) startRaw);
                                    else if (startRaw instanceof String) {
                                        // try parse as long
                                        try {
                                            long l = Long.parseLong((String) startRaw);
                                            startDate = new Date(l);
                                        } catch (Exception ex) {
                                            // not a timestamp — ignore
                                        }
                                    }

                                    String formattedTime = (startDate != null) ? timeFormat.format(startDate) : "";
                                    String displayDate = dateString != null ? dateString : "";

                                    CourseResult r = new CourseResult();
                                    r.sessionId = sessionId;
                                    r.tutorName = tutorFullName;
                                    r.courses = coursesOffered;
                                    r.rating = ratingDouble == null ? null : String.format(Locale.getDefault(),"%.1f", ratingDouble);
                                    r.date = displayDate;
                                    r.time = formattedTime;
                                    r.tutorPhone = tutorPhone;

                                    results.add(r);
                                }
                            } catch (Exception ex) {
                                // ignore single tutor errors
                            } finally {
                                processedCount[0]++;
                                if (processedCount[0] >= totalSessions) finishProcessing();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            processedCount[0]++;
                            if (processedCount[0] >= totalSessions) finishProcessing();
                        }
                    });
                } // end for sessions
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CourseSearchResultsActivity.this, "Failed to load sessions: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            private void finishProcessing() {
                progressBar.setVisibility(View.GONE);
                if (results.isEmpty()) {
                    emptyText.setVisibility(View.VISIBLE);
                    emptyText.setText("No matching sessions found.");
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private String buildTutorName(String first, String last) {
        String f = first == null ? "" : first;
        String l = last == null ? "" : last;
        String full = (f + " " + l).trim();
        return full.isEmpty() ? "Unknown Tutor" : full;
    }

    // Lightweight container for display
    public static class CourseResult {
        public String sessionId;
        public String tutorName;
        public String courses;
        public String rating;
        public String date;
        public String time;
        public String tutorPhone;
    }
}
