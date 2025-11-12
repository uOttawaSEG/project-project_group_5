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

        public TabFilter(TabLayout tl, SessionListAdapter sessionAdapter){
            filter  = null;
            tabLayout = tl;
            sessionListAdapter = sessionAdapter;
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            String tabString = Objects.requireNonNull(tab.getText()).toString();
            Calendar c = Calendar.getInstance();
            if (getString(R.string.pending_session_requests).equals(tabString)) {
                filter = s -> RegistrationStatus.valueOf(s.getSessionStatus()) == RegistrationStatus.PENDING;
            }
            if (getString(R.string.upcoming_sessions).equals(tabString)) {
                filter = s -> s.getStartTime().after(c.getTime());
            }
            if (getString(R.string.past_sessions).equals(tabString)) {
                filter = s -> s.getStartTime().before(c.getTime());
            }
            TutorDashboardActivity.this.getAllSessionsOfTutor(tutorPhoneNumber);
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
                final List<Session> returnList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String phoneNumber = ds.child("tutorPhoneNumber").getValue(String.class);
                         if (tutorPhoneNumber.equals(phoneNumber)) {
                            Session s = ds.getValue(Session.class);
                            returnList.add(s);
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
