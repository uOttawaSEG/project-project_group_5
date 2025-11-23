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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseSearchActivity extends AppCompatActivity {

    private String studentEmail;
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
        studentEmail = intent.getStringExtra("email");
        studentPhoneNumber = intent.getStringExtra("phoneNumber");

        this.courseList = findViewById(R.id.course_search_recycler_view);
        this.searchBar = findViewById(R.id.courseSearchBar);
        this.courseSearchBtn = findViewById(R.id.course_search_button);

        this.cla = new CourseListAdapter(tutorMap, studentPhoneNumber);

        // Load tutors first, THEN allow searching
        getAllTutorsMap();

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

                    // Check if this session has the course and is OPEN
                    if (courses != null && courses.toUpperCase().contains(courseName.toUpperCase()) &&
                            status != null && status.equals(SessionStatus.OPEN.toString())) {
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