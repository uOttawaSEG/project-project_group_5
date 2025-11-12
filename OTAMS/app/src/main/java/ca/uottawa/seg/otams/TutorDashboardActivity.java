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

/**
 * Tutor dashboard: fixed robust date deserialization + tab/filter handling.
 */
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
            filter = null;
            tabLayout = tl;
            sessionListAdapter = sessionAdapter;
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            // Build the filter for the selected tab
            String tabString = Objects.requireNonNull(tab.getText()).toString();
            long now = System.currentTimeMillis();

            if (getString(R.string.pending_session_requests).equals(tabString)) {
                filter = s -> "PENDING".equalsIgnoreCase(s.getSessionStatus());
            } else if (getString(R.string.upcoming_sessions).equals(tabString)) {
                filter = s -> "APPROVED".equalsIgnoreCase(s.getSessionStatus())
                        && s.getStartTime() != null
                        && s.getStartTime().getTime() <= now;
            } else if (getString(R.string.past_sessions).equals(tabString)) {
                filter = s -> "APPROVED".equalsIgnoreCase(s.getSessionStatus())
                        && s.getStartTime() != null
                        && s.getStartTime().getTime() > now;
            } else {
                // fallback: show nothing (or change to show all)
                filter = s -> false;
            }

            // Reload sessions using the new filter
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

        /**
         * Returns the filter. If filter is not set yet, select/initialize the currently selected tab.
         */
        public Predicate<Session> getFilter() {
            if (filter == null) {
                TabLayout.Tab selectedTab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
                if (selectedTab == null) {
                    // select first tab if nothing selected
                    selectedTab = tabLayout.getTabAt(0);
                    if (selectedTab != null) selectedTab.select();
                }
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

        // Ensure a tab is selected and initial data is loaded.
        TabLayout.Tab selected = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
        if (selected == null) {
            TabLayout.Tab first = tabLayout.getTabAt(0);
            if (first != null) {
                first.select(); // triggers onTabSelected -> getAllSessionsOfTutor
            } else {
                // no tabs (?) just load all with a safe filter
                getAllSessionsOfTutor(tutorPhoneNumber);
            }
        } else {
            // ensure data loaded for selected tab
            myTabFilter.onTabSelected(selected);
        }
    }

    /**
     * Load all sessions for tutor and convert start/end times robustly.
     */
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

                        // Robustly read startTime:
                        // - try nested "time" field (Map form),
                        // - then try direct numeric value stored at startTime,
                        // - support Long/Double/Integer.
                        Long startMillis = null;
                        DataSnapshot stTimeNode = ds.child("startTime").child("time");
                        if (stTimeNode.exists()) {
                            Object val = stTimeNode.getValue();
                            startMillis = convertNumberToLong(val);
                        } else {
                            // maybe startTime stored as plain number
                            Object val = ds.child("startTime").getValue();
                            startMillis = convertNumberToLong(val);
                        }
                        if (startMillis != null) s.setStartTime(new Date(startMillis));

                        // Same for endTime
                        Long endMillis = null;
                        DataSnapshot enTimeNode = ds.child("endTime").child("time");
                        if (enTimeNode.exists()) {
                            Object val = enTimeNode.getValue();
                            endMillis = convertNumberToLong(val);
                        } else {
                            Object val = ds.child("endTime").getValue();
                            endMillis = convertNumberToLong(val);
                        }
                        if (endMillis != null) s.setEndTime(new Date(endMillis));

                        // If session is REJECTED, schedule for deletion and skip adding
                        if ("REJECTED".equalsIgnoreCase(s.getSessionStatus())) {
                            rejectedIds.add(s.getId());
                            continue;
                        }

                        allSessions.add(s);
                    }

                    // Remove rejected sessions from DB
                    for (String id : rejectedIds) {
                        sessionsRef.child(id).removeValue();
                    }
                }

                // Apply current tab’s filter (guaranteed non-null by getFilter())
                Predicate<Session> filter = myTabFilter.getFilter();
                List<Session> filtered = allSessions.stream()
                        .filter(filter)
                        .collect(Collectors.toList());
                ula.updateData(filtered);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    /**
     * Helper: convert Firebase numeric types to Long (returns null if not a numeric value).
     */
    private Long convertNumberToLong(Object val) {
        if (val == null) return null;
        if (val instanceof Long) return (Long) val;
        if (val instanceof Integer) return ((Integer) val).longValue();
        if (val instanceof Double) return ((Double) val).longValue();
        // Firebase sometimes returns a String for numbers — try parse
        if (val instanceof String) {
            try {
                return Long.parseLong((String) val);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    public void onClickLogOff(View view) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void onClickManageAvailability(View view) {
        Intent intent = new Intent(TutorDashboardActivity.this, ManageAvailabilityActivity.class);
        intent.putExtra("phoneNumber", tutorPhoneNumber);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh to pick up any status changes (approve/reject).
        getAllSessionsOfTutor(tutorPhoneNumber);
    }
}
