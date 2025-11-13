package ca.uottawa.seg.otams;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TutorDashboardActivity extends AppCompatActivity {

    private RecyclerView recycleView;
    private SessionListAdapter ula;
    private String tutorPhoneNumber;
    private TabFilter myTabFilter;

    private static final String TAG = "TutorDashboard";

    // ---------------------- TAB FILTER ----------------------

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
            String tabString = Objects.requireNonNull(tab.getText()).toString();
            Calendar c = Calendar.getInstance();
            Date now = c.getTime();

            Log.d(TAG, "Tab selected: " + tabString + " | current time = " + now);

            if (getString(R.string.all_sessions).equals(tabString)) {
                // Show all future sessions (any status)
                filter = s -> {
                    Date start = s.getStartTime();
                    boolean result = (start != null && start.after(now));
                    Log.d(TAG, "Filter ALL | " + s.getId() + " | " + s.getSessionStatus() + " | " + start + " | show=" + result);
                    return result;
                };
            } else if (getString(R.string.upcoming_sessions).equals(tabString)) {
                // Only APPROVED and future sessions
                filter = s -> {
                    Date start = s.getStartTime();
                    boolean result = (start != null && start.after(now) &&
                            "OPEN".equalsIgnoreCase(s.getSessionStatus()));
                    Log.d(TAG, "Filter UPCOMING | " + s.getId() + " | " + s.getSessionStatus() + " | " + start + " | show=" + result);
                    return result;
                };
            } else if (getString(R.string.past_sessions).equals(tabString)) {
                // Only APPROVED and past sessions
                filter = s -> {
                    Date start = s.getStartTime();
                    boolean result = (start != null && start.before(now) &&
                            "OPEN".equalsIgnoreCase(s.getSessionStatus()));
                    Log.d(TAG, "Filter PAST | " + s.getId() + " | " + s.getSessionStatus() + " | " + start + " | show=" + result);
                    return result;
                };
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

    // ---------------------- ON CREATE ----------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tutor_dashboard);

        Intent intent = getIntent();
        tutorPhoneNumber = intent.getStringExtra("phoneNumber");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.back_button), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recycleView = findViewById(R.id.session_request_recycler_view);
        ula = new SessionListAdapter(new ArrayList<>());

        TabLayout td = findViewById(R.id.tutor_tab_layout);
        myTabFilter = new TabFilter(td, ula);
        td.addOnTabSelectedListener(myTabFilter);

        getAllSessionsOfTutor(tutorPhoneNumber);
    }

    // ---------------------- FIREBASE FETCH ----------------------

    private void getAllSessionsOfTutor(String tutorPhoneNumber) {
        DatabaseReference sessionsReference = FirebaseDatabase.getInstance().getReference("sessions");
        Query tutorSessions = sessionsReference.orderByChild("tutorPhoneNumber").equalTo(tutorPhoneNumber);

        tutorSessions.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Session> returnList = new ArrayList<>();

                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Session s = ds.getValue(Session.class);
                        if (s == null) continue;

                        if (!tutorPhoneNumber.equals(s.getTutorPhoneNumber())) continue;

                        // Confirm proper Date conversion
                        if (s.getStartTime() == null) {
                            Log.w(TAG, "Session " + s.getId() + " has null startTime");
                            continue;
                        }

                        returnList.add(s);
                    }
                }

                Log.d(TAG, "Loaded " + returnList.size() + " sessions for tutor " + tutorPhoneNumber);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                for (Session s : returnList) {
                    Log.d(TAG, "Session loaded: " + s.getId()
                            + " | status=" + s.getSessionStatus()
                            + " | start=" + sdf.format(s.getStartTime()));
                }

                populateRecyclerView(filterSessionBy(returnList, myTabFilter.getFilter()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading sessions: " + error.getMessage());
            }
        });
    }

    // ---------------------- HELPER FUNCTIONS ----------------------

    private static List<Session> filterSessionBy(List<Session> sessionList, Predicate<Session> filterMechanism) {
        return sessionList.stream().filter(filterMechanism).collect(Collectors.toList());
    }

    private void populateRecyclerView(List<Session> sessionsList) {
        if (ula.getSessionList().isEmpty()) {
            RecyclerView rv = this.recycleView;
            rv.setLayoutManager(new LinearLayoutManager(this));
            ula.getSessionList().addAll(sessionsList);
            rv.setAdapter(ula);
        } else {
            ula.updateData(sessionsList);
        }
    }

    // ---------------------- BUTTON HANDLERS ----------------------

    public void onClickLogOff(View view) {
        startActivity(new Intent(TutorDashboardActivity.this, MainActivity.class));
    }

    public void onClickManageAvailability(View view) {
        Intent intent = new Intent(TutorDashboardActivity.this, ManageAvailabilityActivity.class);
        intent.putExtra("phoneNumber", tutorPhoneNumber);
        startActivity(intent);
    }

    // ---------------------- REFRESH HANDLER ----------------------

    @Override
    protected void onResume() {
        super.onResume();
        getAllSessionsOfTutor(tutorPhoneNumber);
    }
}
