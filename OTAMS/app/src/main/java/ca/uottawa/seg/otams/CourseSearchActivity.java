package ca.uottawa.seg.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseSearchActivity extends AppCompatActivity {

    private String studentName;
    private String studentPhoneNumber;
    private RecyclerView courseList;
    private CourseListAdapter cla;

    private EditText searchBar;
    private Button courseSearchBtn;

    private Map<String, Tutor> tutorMap = new HashMap<>();
    private boolean tutorsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_search);

        Intent intent = getIntent();
        studentName = intent.getStringExtra("name");
        studentPhoneNumber = intent.getStringExtra("phoneNumber");

        this.courseList = findViewById(R.id.course_search_recycler_view);
        this.searchBar = findViewById(R.id.courseSearchBar);
        this.courseSearchBtn = findViewById(R.id.course_search_button);

        this.cla = new CourseListAdapter(tutorMap, studentPhoneNumber, studentName, this::onSessionBooked);

        // Load tutors and student's existing bookings
        getAllTutorsMap();
        getStudentBookings();

        this.courseSearchBtn.setOnClickListener(v -> {
            String searchText = this.searchBar.getText().toString();
            if (searchText.isBlank()) {
                populateRecyclerView(List.of());
            } else {
                findSessionsByCourseName(searchText);
            }
        });
    }

    private List<Session> findSessionsByCourseName(String courseName) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("sessions");
        List<Session> results = new ArrayList<>();

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                results.clear();
                for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                    String courses = sessionSnapshot.child("courses").getValue(String.class);
                    String status = sessionSnapshot.child("sessionStatus").getValue(String.class);

                    // Check if the current session is for the typed course and is open
                    if (courses != null && courseMatchesExactly(courses, courseName) && status != null && status.equals(SessionStatus.OPEN.toString())) {
                        Session session = sessionSnapshot.getValue(Session.class);
                        if (session != null) {
                            results.add(session);
                        }
                    }
                }
                populateRecyclerView(results);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                populateRecyclerView(new ArrayList<>());
            }
        });

        return results;
    }

    // Check if course is being taught by that tutor in that session
    private boolean courseMatchesExactly(String coursesString, String searchTerm) {
        String[] courseArray = coursesString.split("\\s+");
        for (String course : courseArray) {
            if (course.equalsIgnoreCase(searchTerm.trim())) {
                return true;
            }
        }
        return false;
    }

    private void getAllTutorsMap() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tutorMap.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userRole = userSnapshot.child("role").getValue(String.class);

                    // Only process tutors
                    if (userRole != null && userRole.equals("Tutor")) {
                        Tutor tutor = userSnapshot.getValue(Tutor.class);
                        if (tutor != null) {
                            String tutorName = tutor.getFirstName() + " " + tutor.getLastName();
                            tutorMap.put(tutorName, tutor);
                        }
                    }
                }
                tutorsLoaded = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tutorsLoaded = false;
            }
        });
    }

    private void getStudentBookings() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("sessions");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentBookings.clear();
                for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                    Session session = sessionSnapshot.getValue(Session.class);
                    if (session != null) {
                        String status = sessionSnapshot.child("sessionStatus").getValue(String.class);
                        String studentPhone = sessionSnapshot.child("studentPhoneNumber").getValue(String.class);

                        // Get student bookings that are PENDING or APPROVED
                        if (studentPhone != null && studentPhone.equals(studentPhoneNumber) &&
                                status != null && (status.equals(SessionStatus.PENDING.toString()) ||
                                status.equals(SessionStatus.APPROVED.toString()))) {
                            studentBookings.add(session);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public boolean hasTimeConflict(Date startTime, Date endTime) {
        for (Session booking : studentBookings) {
            // Check if there's an overlap
            if (!(endTime.before(booking.getStartTime()) || startTime.after(booking.getEndTime()))) {
                return true; // Overlap found
            }
        }
        return false;
    }

    // Callback when a session is successfully booked
    private void onSessionBooked(Session bookedSession) {
        // Remove the booked session from results in real time
        cla.removeSession(bookedSession);

        // Add to student's bookings list
        studentBookings.add(bookedSession);
    }

    private void populateRecyclerView(List<Session> courseList) {
        if (cla.getCourseSession().isEmpty()) {
            RecyclerView rv = this.courseList;
            rv.setLayoutManager(new LinearLayoutManager(this));
            cla.getCourseSession().addAll(courseList);
            rv.setAdapter(cla);
        } else {
            if (courseList.isEmpty()) {
                cla.clearData();
            } else {
                cla.updateData(courseList);
            }
        }
    }

    public void onClickBackToDashboard(View view) {
        int pressID = view.getId();
        if (pressID == R.id.button_backToStudentDashboard) {
            finish();
        }
    }
}