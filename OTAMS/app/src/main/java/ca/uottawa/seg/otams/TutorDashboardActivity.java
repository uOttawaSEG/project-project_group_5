package ca.uottawa.seg.otams;

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

public class TutorDashboardActivity extends AppCompatActivity {

    //private RegistrationStatus rs = null;
    private RecyclerView recycleView;
    private SessionListAdapter ula;
    String tutorPhoneNumber; // Stores the phone number of the tutor so their entry in the database can quickly be found (since phone number is the key)

    private TabFilter myTabFilter;
    private class TabFilter implements TabLayout.OnTabSelectedListener {

        private Predicate<Session> filter;
        private final TabLayout tabLayout;

        private final SessionListAdapter sessionListAdapter;

        public TabFilter(TabLayout tl, SessionListAdapter sessionAdapter) {
            filter  = null;
            tabLayout = tl;
            sessionListAdapter = sessionAdapter;
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            String tabString = Objects.requireNonNull(tab.getText()).toString();
            Calendar c = Calendar.getInstance();
            Date now = c.getTime();

            if (getString(R.string.all_sessions).equals(tabString)) {
                // filter = s -> RegistrationStatus.valueOf(s.getSessionStatus()) == RegistrationStatus.PENDING;
                // filter = s -> s.getStartTime().after(c.getTime())&&RegistrationStatus.valueOf(s.getSessionStatus()) == RegistrationStatus.APPROVED;

                // Shows all sessions that have not yet passed and have not been rejected or cancelled (i.e. approved, pending and open session slots)
                filter = s -> s.getEndTime().after(now) && !s.getSessionStatus().equals("REJECTED") && !s.getSessionStatus().equals("CANCELLED");
            }
            else if (getString(R.string.upcoming_sessions).equals(tabString)) {
                // filter = s -> s.getStartTime().after(c.getTime())&&RegistrationStatus.valueOf(s.getSessionStatus()) == RegistrationStatus.APPROVED;

                // Shows all sessions that have been booked by a student and approved by the tutor and have yet to happen
                filter = s -> s.getEndTime().after(now) && s.getSessionStatus().equals("APPROVED");
            }
            else if (getString(R.string.past_sessions).equals(tabString)) {
                // filter = s -> s.getStartTime().before(c.getTime())&&RegistrationStatus.valueOf(s.getSessionStatus()) == RegistrationStatus.APPROVED;

                // Shows all sessions that the tutor has already finished
                filter = s -> s.getEndTime().before(now) && s.getSessionStatus().equals("APPROVED");
            }

            TutorDashboardActivity.this.getAllSessionsOfTutor(tutorPhoneNumber);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            sessionListAdapter.clearData();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_tutor_dashboard);

        // Stores the phone number of the tutor that was passed from the previous activity
        Intent intent = getIntent();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.back_button), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tutorPhoneNumber = intent.getStringExtra("phoneNumber");
        recycleView = findViewById(R.id.session_request_recycler_view);
        ula = new SessionListAdapter(new ArrayList<>());
        final TabLayout td = findViewById(R.id.tutor_tab_layout);
        myTabFilter = new TabFilter(td, ula);
        td.addOnTabSelectedListener(myTabFilter);
        getAllSessionsOfTutor(tutorPhoneNumber);
    }

    private static List<Session> filterSessionBy(List<Session> sessionList, Predicate<Session> filterMechanism) {
            return sessionList.stream().filter(filterMechanism).collect(Collectors.toList());
    }

    private void getAllSessionsOfTutor(String tutorPhoneNumber) {
        // Get all sessions from Firebase
        DatabaseReference sessionsReference = FirebaseDatabase.getInstance().getReference("sessions");
        Query tutorSessions = sessionsReference.orderByChild("tutorPhoneNumber").equalTo(tutorPhoneNumber);

        tutorSessions.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Session> returnList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String phoneNumber = ds.child("tutorPhoneNumber").getValue(String.class);
                         if (tutorPhoneNumber.equals(phoneNumber)) {
                             // If the session found in the database belongs to the tutor then create a session object from it (save the session)
                            Session s = ds.getValue(Session.class);

                            if (s != null) {
                                // Fetch the start/end timestamps stored in the database
                                DataSnapshot startTimeSnapshot = ds.child("startTime");
                                DataSnapshot endTimeSnapshot = ds.child("endTime");

                                // Variables that will hold the start/end timestamps fetched from the database as Date objects
                                Date startTime = null;
                                Date endTime = null;

                                if (startTimeSnapshot.exists()) {
                                    // If there is a start time stored in the database (should always be true), then fetch and store its milliseconds representation
                                    Long startTimeInMillis = startTimeSnapshot.child("time").getValue(Long.class);

                                    if (startTimeInMillis != null) {
                                        // If this representation in the database is valid (should always be), then create a Date object from it
                                        startTime = new Date(startTimeInMillis);
                                    }
                                }

                                if (endTimeSnapshot.exists()) {
                                    // If there is a start time stored in the database (should always be true), then fetch and store its milliseconds representation
                                    Long endTimeInMillis = endTimeSnapshot.child("time").getValue(Long.class);

                                    if (endTimeInMillis != null) {
                                        // If this representation in the database is valid (should always be), then create a Date object from it
                                        endTime = new Date(endTimeInMillis);
                                    }
                                }

                                // Set the start and end times of the session inside the session object representing it to these Date objects.
                                // This is necessary since Firebase stores the start and end times as non Date objects (technically) that are
                                // not properly converted and saved inside a session
                                s.setStartTime(startTime);
                                s.setEndTime(endTime);

                                // Add the created session to the list storing all of this tutor's sessions
                                returnList.add(s);
                            }
                        }
                    }
                }
                populateRecyclerView(filterSessionBy(returnList, myTabFilter.getFilter()));
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

    public void onClickLogOff(View view) {
        // Set the next page to the login page
        Intent intent = new Intent(TutorDashboardActivity.this, MainActivity.class);

        // Send the user to the login page
        startActivity(intent);
    }

    public void onClickManageAvailability(View view) {

        int pressID=view.getId();

        // Check if the user is trying to access the manage availability page
        if (pressID == R.id.button_manage_availability) {
            // Set the next page to the manage availability page
            Intent intent = new Intent(TutorDashboardActivity.this, ManageAvailabilityActivity.class);

            // Pass the tutor's phone number to the next page so that the tutor can quickly be identified and found in the database (since the phone number is the key)
            intent.putExtra("phoneNumber", tutorPhoneNumber);

            // Send the user to the manage availability page (i.e. the one with the calender)
            startActivity(intent);
        }

    }

    // Refresh the RecyclerView (request inbox) whenever the tutor returns back to their dashboard
    @Override
    protected void onResume() {
        super.onResume();

        // Determines which tab (Pending Session Requests, Upcoming Sessions or Past Sessions) the tutor is currently on in the dashboard
        // Add code here

        // Refreshes the request inbox for the selected tab
        // Add code in here
    }
}
