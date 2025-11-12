package ca.uottawa.seg.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TutorDashboardActivity extends AppCompatActivity {

    private RecyclerView recycleView;
    private SessionListAdapter ula;
    private String tutorPhoneNumber;
    private TabFilter myTabFilter;

    private class TabFilter implements TabLayout.OnTabSelectedListener {

        private Predicate<Session> filter;
        private final TabLayout tabLayout;
        private final SessionListAdapter sessionListAdapter;

        public TabFilter(TabLayout tl, SessionListAdapter sessionAdapter) {
            this.tabLayout = tl;
            this.sessionListAdapter = sessionAdapter;
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            String tabString = Objects.requireNonNull(tab.getText()).toString();
            filter = null;

            if (tabString.equals(getString(R.string.pending_session_requests))) {
                filter = s -> RegistrationStatus.PENDING.name().equalsIgnoreCase(s.getSessionStatus());
            } else if (tabString.equals(getString(R.string.upcoming_sessions))) {
                filter = s -> RegistrationStatus.APPROVED.name().equalsIgnoreCase(s.getSessionStatus())
                        && s.getStartTime() != null
                        && s.getStartTime().after(new Date());
            } else if (tabString.equals(getString(R.string.past_sessions))) {
                filter = s -> RegistrationStatus.APPROVED.name().equalsIgnoreCase(s.getSessionStatus())
                        && s.getStartTime() != null
                        && s.getStartTime().before(new Date());
            }

            getAllSessionsOfTutor(tutorPhoneNumber);
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
                TabLayout.Tab selectedTab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
                if (selectedTab != null) onTabSelected(selectedTab);
            }
            return filter;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tutor_dashboard);

        tutorPhoneNumber = getIntent().getStringExtra("phoneNumber");

        recycleView = findViewById(R.id.session_request_recycler_view);
        ula = new SessionListAdapter(new ArrayList<>());
        recycleView.setLayoutManager(new LinearLayoutManager(this));
        recycleView.setAdapter(ula);

        TabLayout tabLayout = findViewById(R.id.tutor_tab_layout);
        myTabFilter = new TabFilter(tabLayout, ula);
        tabLayout.addOnTabSelectedListener(myTabFilter);

        getAllSessionsOfTutor(tutorPhoneNumber);
    }

    private void getAllSessionsOfTutor(String tutorPhoneNumber) {
        DatabaseReference sessionsRef = FirebaseDatabase.getInstance().getReference("sessions");
        Query tutorSessions = sessionsRef.orderByChild("tutorPhoneNumber").equalTo(tutorPhoneNumber);

        tutorSessions.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Session> allSessions = new ArrayList<>();
                List<String> rejectedIds = new ArrayList<>();

                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Session s = ds.getValue(Session.class);
                        if (s == null) continue;

                        // Deserialize startTime and endTime from timestamp
                        Object startObj = ds.child("startTime").child("time").getValue();
                        if (startObj instanceof Long) s.setStartTime(new Date((Long) startObj));

                        Object endObj = ds.child("endTime").child("time").getValue();
                        if (endObj instanceof Long) s.setEndTime(new Date((Long) endObj));

                        // Delete REJECTED sessions
                        if (RegistrationStatus.REJECTED.name().equalsIgnoreCase(s.getSessionStatus())) {
                            rejectedIds.add(s.getId());
                            continue;
                        }

                        allSessions.add(s);
                    }

                    // Remove rejected sessions from Firebase
                    for (String id : rejectedIds) {
                        sessionsRef.child(id).removeValue();
                    }
                }

                List<Session> filtered = allSessions.stream()
                        .filter(myTabFilter.getFilter())
                        .collect(Collectors.toList());
                ula.updateData(filtered);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void onClickLogOff(View view) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void onClickManageAvailability(View view) {
        Intent intent = new Intent(this, ManageAvailabilityActivity.class);
        intent.putExtra("phoneNumber", tutorPhoneNumber);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAllSessionsOfTutor(tutorPhoneNumber);
    }
}
