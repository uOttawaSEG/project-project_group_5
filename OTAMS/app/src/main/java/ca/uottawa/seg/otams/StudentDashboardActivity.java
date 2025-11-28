package ca.uottawa.seg.otams;

import static android.content.Intent.getIntent;
import static android.provider.Settings.System.getString;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StudentDashboardActivity  extends AppCompatActivity{
    private RecyclerView recycleView;
    private SessionListAdapter ula;
    String studentPhoneNumber;

    private TabFilter myTabFilter;

    private class TabFilter implements TabLayout.OnTabSelectedListener {

        private Predicate<Session> filter;
        private final TabLayout tabLayout;

        private final SessionListAdapter sessionListAdapter;



        public TabFilter(TabLayout tl, SessionListAdapter sessionAdapter){
            filter  = null;
            tabLayout = tl;
            sessionListAdapter = sessionAdapter;
        }

        //Called whenever activity loads on screen
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            String tabString = Objects.requireNonNull(tab.getText()).toString();
            Calendar c = Calendar.getInstance();
            SessionStatus sessionStatus = SessionStatus.PENDING;
            if (getString(R.string.student_upcoming_sessions).equals(tabString)) {
                sessionStatus = SessionStatus.APPROVED;
            } else
            if (getString(R.string.student_past_sessions).equals(tabString)) {
                sessionStatus = SessionStatus.COMPLETED;
            } else if (getString(R.string.rejected_sessions).equals(tabString)) {
                sessionStatus = SessionStatus.REJECTED;
            }
            StudentDashboardActivity.this.filterSessionBy(sessionStatus);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            this.sessionListAdapter.clearData();
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            onTabSelected(tab);
        }

        public Predicate<Session> getFilter() {
            if (filter == null) {
                TabLayout.Tab selectedTab = this.tabLayout.getTabAt(this.tabLayout.getSelectedTabPosition());
                if (selectedTab == null) {
                    selectedTab = tabLayout.getTabAt(0);
                    selectedTab.select();
                }
                onTabSelected(selectedTab);
            }
            return filter;
        }
    }

    private SessionStatus getSessionStatus(TabLayout.Tab selectedTab) {
        String tabString = selectedTab.getText().toString();
        if (tabString.equals(getString(R.string.pending_sessions))) {
            return SessionStatus.PENDING;
        }
        if (tabString.equals(getString(R.string.rejected_requests))) {
            return SessionStatus.REJECTED;
        }
        if (tabString.equals(getString(R.string.past_sessions))) {
            return SessionStatus.COMPLETED;
        }
        return SessionStatus.APPROVED;

    }
    private SessionStatus getSessionStatus(TabLayout tb) {
        TabLayout.Tab selectedTab = tb.getTabAt(tb.getSelectedTabPosition());
        if (selectedTab == null) {
            selectedTab = tb.getTabAt(0);
            selectedTab.select();
        }
        return getSessionStatus(selectedTab);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.back_button), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = getIntent();
        studentPhoneNumber = intent.getStringExtra("phoneNumber");
        recycleView = findViewById(R.id.session_request_recycler_view);
        ula = new SessionListAdapter(new ArrayList<>());
        final TabLayout td = findViewById(R.id.student_tab_layout);
        myTabFilter = new StudentDashboardActivity.TabFilter(td, ula);
        td.addOnTabSelectedListener(myTabFilter);
        //Automatically open on pending
        filterSessionBy(SessionStatus.PENDING);
    }

    private static List<Session> filterSessionBy(List<Session> sessionList, Predicate<Session> filterMechanism) {
        return filterMechanism == null ? sessionList : sessionList.stream().filter(filterMechanism).collect(Collectors.toList());
    }

    private void filterSessionBy(SessionStatus sessionStatus) {
        // Get all sessions from Firebase
        DatabaseReference sessionsReference = FirebaseDatabase.getInstance().getReference("sessions");
        Query tutorSessions = sessionsReference.orderByChild("phoneNumber").equalTo(studentPhoneNumber);
        tutorSessions.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Session> sessionList = new ArrayList<>(); // List containing the details of every session for this user

                // If a session exists in the database with the specified session status
                if(snapshot.exists()) {

                    // Iterate through every session in the database with the specified session status and adds it to the corresponding session inbox in the student dashboard
                    for (DataSnapshot ds : snapshot.getChildren()) {

                        // Then fetch the session status
                        String requestStatusFromDatabase = ds.child("requestStatus").getValue(String.class);
                        // Fetch the student's email
                        String studentPhoneNumberCheck = ds.child("studentPhoneNumber").getValue(String.class);

                        if (studentPhoneNumber.equals(studentPhoneNumberCheck) && sessionStatus.toString().equals(requestStatusFromDatabase)) {
                            Session s = ds.getValue(Session.class);
                            sessionList.add(s);
                        }
                    }
                }

                populateRecyclerView(filterSessionBy(sessionList, myTabFilter.getFilter()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }

        });
    }


            private void populateRecyclerView(List<Session> sessionsList) {
        if (ula.getSessionList().isEmpty()) {
            // If the inbox is being populated for the first time then create and adapter for it
            RecyclerView rv = this.recycleView;
            rv.setLayoutManager(new LinearLayoutManager(this));
            ula.getSessionList().addAll(sessionsList);
            rv.setAdapter(ula);
        } else {
            // If the adapter already exists then just update it instead of creating a new one
            ula.updateData(sessionsList);
        }
    }

    public void onClickBackToWelcomePage(View view) {
        // Set the next page to the login page
        if (view.getId() == R.id.student_dashboard_back_button) {
            finish();
        }
    }

    public void onClickLogOff(View view) {
        // Set the next page to the login page
        if (view.getId() == R.id.log_off) {
            Intent intent = new Intent(StudentDashboardActivity.this, MainActivity.class);

            // Send the user to the login page
            startActivity(intent);
        }
    }

    // Refresh the RecyclerView (request inbox) whenever the tutor returns back to their dashboard
    @Override
    protected void onResume() {
        super.onResume();

        // Determines which tab (Pending Session Requests, Upcoming Sessions or Past Sessions) the tutor is currently on in the dashboard
        TabLayout tb = findViewById(R.id.student_tab_layout);

        // Refreshes the request inbox for the selected tab
        filterSessionBy(getSessionStatus(tb));
    }
}


