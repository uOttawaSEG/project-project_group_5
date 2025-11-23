package ca.uottawa.seg.otams;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CourseResultsActivity extends AppCompatActivity {

    private String studentPhoneNumber;
    private String courseCode;
    private RecyclerView recyclerView;
    private List<Session> sessionList;
    private CourseResultsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_results);

        // Receive data from previous activity
        studentPhoneNumber = getIntent().getStringExtra("phoneNumber");
        courseCode = getIntent().getStringExtra("courseCode");

        recyclerView = findViewById(R.id.resultsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        sessionList = new ArrayList<>();

        adapter = new CourseResultsAdapter(sessionList, session -> {
            // Handle booking logic here later
            Toast.makeText(this, "Clicked Book for session " + session.getId(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);

        loadSessions();
    }

    private void loadSessions() {
        DatabaseReference tutorsRef = FirebaseDatabase.getInstance().getReference("users");
        tutorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sessionList.clear();

                for (DataSnapshot tutorSnap : snapshot.getChildren()) {
                    String courses = tutorSnap.child("coursesOffered").getValue(String.class);
                    if (courses != null && courses.toLowerCase().contains(courseCode.toLowerCase())) {
                        String tutorPhone = tutorSnap.child("phoneNumber").getValue(String.class);
                        String tutorName = tutorSnap.child("firstName").getValue(String.class) + " " +
                                tutorSnap.child("lastName").getValue(String.class);
                        Long rating = tutorSnap.child("rating").getValue(Long.class);

                        // Now check sessions
                        DatabaseReference sessionsRef = FirebaseDatabase.getInstance().getReference("sessions");
                        sessionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot sessionSnap) {
                                for (DataSnapshot sSnap : sessionSnap.getChildren()) {
                                    String sessionTutorPhone = sSnap.child("tutorPhoneNumber").getValue(String.class);
                                    String sessionStatus = sSnap.child("sessionStatus").getValue(String.class);

                                    if (tutorPhone.equals(sessionTutorPhone) &&
                                            (sessionStatus.equals("OPEN") || sessionStatus.equals("PENDING"))) {
                                        // Create a Session object (we'll just fill minimal fields for now)
                                        Session s = new Session();
                                        s.setTutorName(tutorName);
                                        s.setTutorPhoneNumber(tutorPhone);
                                        s.setDate(sSnap.child("date").getValue(String.class));
                                        s.setSessionStatus(SessionStatus.valueOf(sessionStatus));
                                        sessionList.add(s);
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
