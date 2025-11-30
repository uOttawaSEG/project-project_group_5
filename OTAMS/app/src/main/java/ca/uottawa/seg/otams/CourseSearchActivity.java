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
import java.util.Calendar;
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

    private Map<String, Tutor> tutorMap = new HashMap<>(); // Stores tutor's info inside a map with the tutor's phone number as the key (since its unique for each tutor)
    private List<Session> studentSessions = new ArrayList<>(); // Stores all of the students currently pending or approved sessions
    private boolean tutorsLoaded = false; // Determines whether the tutor's data has been loaded from the database yet

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_search);

        // Receive the data passed from the previous page (i.e. the student's full name and phone number)
        Intent intent = getIntent();
        studentName = intent.getStringExtra("name");
        studentPhoneNumber = intent.getStringExtra("phoneNumber");

        this.courseList = findViewById(R.id.course_search_recycler_view);
        this.searchBar = findViewById(R.id.courseSearchBar);
        this.courseSearchBtn = findViewById(R.id.course_search_button);

        this.cla = new CourseListAdapter(tutorMap, studentPhoneNumber, studentName, this, this::onSessionBooked);

        // Fetch all tutors' data and all the student's existing pending and approved sessions from the database
        getAllTutorsMap();
        getStudentSessions();

        // Populate the page with open session slots for the course code specified in the search bar by the student when the search button is clicked
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
                results.clear(); // Clear previous search results
                long currentTimeMillis = System.currentTimeMillis(); // Get the current time

                for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                    String courses = sessionSnapshot.child("courses").getValue(String.class);
                    String status = sessionSnapshot.child("sessionStatus").getValue(String.class);

                    // Check if the current session matches the course code and is open
                    if (courses != null && courseMatchesExactly(courses, courseName) && status != null && status.equals(SessionStatus.OPEN.toString())) {

                        Session session = sessionSnapshot.getValue(Session.class);

                        if (session != null) {
                            // Reconstruct the start and end times from Firebase
                            Date startTime = reconstructSessionDate(sessionSnapshot.child("startTime"));
                            Date endTime = reconstructSessionDate(sessionSnapshot.child("endTime"));

                            session.setStartTime(startTime);
                            session.setEndTime(endTime);

                            // Only add the session if it hasn't started yet
                            if (startTime != null && startTime.getTime() > currentTimeMillis) {
                                results.add(session);
                            }
                        }
                    }
                }

                // Sort sessions by start time (soonest first)
                sortSessionsByDate(results);
                populateRecyclerView(results);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                populateRecyclerView(new ArrayList<>());
            }
        });

        return results;
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

    // Sorts sessions by start time with the soonest sessions first
    private void sortSessionsByDate(List<Session> sessionList) {
        sessionList.sort((s1, s2) -> {
            if (s1.getStartTime() == null && s2.getStartTime() == null) {
                return 0;
            }
            if (s1.getStartTime() == null) {
                return 1; // s1 goes to the end
            }
            if (s2.getStartTime() == null) {
                return -1; // s2 goes to the end
            }
            return s1.getStartTime().compareTo(s2.getStartTime());
        });
    }

    private boolean courseMatchesExactly(String coursesString, String searchTerm) {
        String[] courseArray = coursesString.split("\\s+"); // Separates the course codes by whitespace (i.e. spaces) into multiple smaller strings
        for (String course : courseArray) {
            // Loop through each course listed and see if it matches the course code entered by the student (is a case-insensitive comparison to ensure that it does not matter whether the course code was typed in upper or lower case)
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

                    // Look through user entries in the database that are for tutors only
                    if (userRole != null && userRole.equals("Tutor")) {
                        Tutor tutor = userSnapshot.getValue(Tutor.class);
                        if (tutor != null) {
                            String tutorPhoneNumber = tutor.getPhoneNumber(); // Get the phone number of the tutor
                            tutorMap.put(tutorPhoneNumber, tutor); // Store the tutor's info inside the map to easily access their info with the phone number being the key
                        }
                    }
                }
                tutorsLoaded = true; // Indicates that the tutors' info has finished being loaded from the database
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tutorsLoaded = false;
            }
        });
    }

    private void getStudentSessions() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("sessions");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentSessions.clear();
                for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                    Session session = sessionSnapshot.getValue(Session.class);
                    if (session != null) {
                        String status = sessionSnapshot.child("sessionStatus").getValue(String.class);
                        String studentPhone = sessionSnapshot.child("studentPhoneNumber").getValue(String.class);

                        // Get all of the student's sessions that are pending or approved
                        if (studentPhone != null && studentPhone.equals(studentPhoneNumber) && status != null && (status.equals(SessionStatus.PENDING.toString()) || status.equals(SessionStatus.APPROVED.toString()))) {

                            // Reconstruct the dates for time conflict checking
                            Date startTime = reconstructSessionDate(sessionSnapshot.child("startTime"));
                            Date endTime = reconstructSessionDate(sessionSnapshot.child("endTime"));
                            session.setStartTime(startTime);
                            session.setEndTime(endTime);

                            studentSessions.add(session); // Add it to the list if it is
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
        for (Session session : studentSessions) {
            // Loop through every pending and approved session that the student currently has and check if it overlaps with the session the student is currently trying to book
            if (!(endTime.before(session.getStartTime()) || startTime.after(session.getEndTime()))) {
                return true;
            }
        }
        return false;
    }

    // Called when a session is successfully booked
    private void onSessionBooked(Session bookedSession) {
        // Removes the session from the search results in real time
        cla.removeSession(bookedSession);

        // Add the session to the list containing all of the student's currently pending and approved sessions (since it must also now be checked for overlaps, even without refreshing the page)
        studentSessions.add(bookedSession);
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
            finish(); // If the dashboard button is clicked, then send the student back to the dashboard
        }
    }
}