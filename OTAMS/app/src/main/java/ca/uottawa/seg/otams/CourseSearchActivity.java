package ca.uottawa.seg.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_search);

        // Stores the email of the student that logged in that was passed from the previous activity
        Intent intent = getIntent();
        studentEmail = intent.getStringExtra("email");
        studentPhoneNumber = intent.getStringExtra("phoneNumber");
        this.courseList = findViewById(R.id.course_search_recycler_view);
        this.searchBar = findViewById(R.id.courseSearchBar);
        this.courseSearchBtn = findViewById(R.id.course_search_button);

        // this.cla = new CourseListAdapter(getAllTutorsMap(this.searchBar.getText().toString()));
        this.cla = new CourseListAdapter(new HashMap<>(), studentPhoneNumber);
        getAllTutorsMap(this.searchBar.getText().toString());

        this.courseSearchBtn.setOnClickListener(v -> {
            String searchText = this.searchBar.getText().toString();
            if (searchText.isBlank()) populateRecyclerView(List.of()); else populateRecyclerView(findSessionsByCourseName(searchText));
        });

        /*//KEEP THIS COMMENTED OUT BECAUSE IT BREAKS THE APP: when user clicks go to course search page, this piece of code makes it log out instead and you will never be able to access the actual page

        //search bar little bit of code
        SearchView courseSearchBar = findViewById(R.id.courseSearchBar);
        courseSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            //when user presses enter after inputting course code
            public boolean onQueryTextSubmit(String courseInput) {
                //put stuff here like getting the tutor info using the courseInput from search bar
                return true;
            }

            //when user is typing don't change the page or do anything
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        */

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

        // TO-DO

        // return List.of();
    }

    Map<String, Tutor> getAllTutorsMap(String courseName) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Map<String, Tutor> tutorMap = new HashMap<>();

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        return tutorMap;

        // TO-DO

        // return Map.of();
    }

    private void populateRecyclerView(List<Session> courseList) {
        if (cla.getCourseSession().isEmpty()) {
            // If the inbox is being populated for the first time then create and adapter for it
            RecyclerView rv = this.courseList;
            rv.setLayoutManager(new LinearLayoutManager(this));
            cla.getCourseSession().addAll(courseList);
            rv.setAdapter(cla);
        } else {
            // If the adapter already exists then just update it instead of creating a new one
            if (courseList.isEmpty()) {
                cla.clearData();
            } else {
                cla.updateData(courseList);
            }
        }
    }


    /*these are some ideas I was thinking about when creating the frontend. i put it in a comment just to explain what i did and hopefully guide the backend into what is going on - feel free to change it though!:

    visibility in frontend:- backend not yet implemented
    - when nothing is searched, no text or session list shows up
    - when searched, the courseResultsTitle and course_search_recycler_view ids in the activity_course_search xml file will show up. Right now they are hidden since nothing has been searched yet, but will need to change since currently android:visibility="gone"
    - i have courseResultsTitle as text that will appear after the search so the user knows what they searched for, in case they entered a new course code and didn't search it, they will at least know they are still stuck on previous search.
       -> i added 5 underscores as a blank area so that the course that was searched can be displayed there instead. not sure if we want this feature, and if so, it will need to be implemented in the backend i think

    search bar and searches: - backend not yet implemented
    - the search bar has light purple text that tells the user to enter a course code
    - once a user starts typing, the light-coloured prompt disappears and the actual input appears
    - user presses enter on keyboard to submit the input course code they entered
    - the code below is what happens after the enter key is pressed. this needs to be filled out with the backend logic of getting the data etc

    results and pulling from data: - backend not yet implemented
    - once the search enter is clicked, the results show up as recycle view
    - it shows tutor name, date, time, courses offered, rating, and a book button
    - the frontend of it is in activity_list_course_results. i will update the spacing of it later once i get an idea of how it looks when there is info
    - the info from the firebase database will show up in these fields: tutor name, date, time, courses

    average rating:- backend not sure if already implemented, ask Zahabia since she worked on rating front-end
    - not sure how rating will be calculated. check the rating page? will it be stored somewhere?

    book button: - backend not yet implemented
    - the book button will book the session and should then remove it from results - not yet implemented
    */







    //return to dashboard button
    public void onClickBackToDashboard(View view) {
        int pressID=view.getId();

        // Check if the student is trying to return to their dashboard
        if (pressID == R.id.button_backToStudentDashboard) {
            // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
            finish();
        }
    }

}
