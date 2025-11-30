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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

public class StudentDashboardActivity extends AppCompatActivity {
    private RecyclerView recycleView;
    private SessionListAdapter ula;
    String studentPhoneNumber;
    String studentEmail;

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
            String tabString = Objects.requireNonNull(tab.getText()).toString();
            SessionStatus sessionStatus = SessionStatus.PENDING;
            if (getString(R.string.student_upcoming_sessions).equals(tabString)) {
                sessionStatus = SessionStatus.APPROVED;
            } else if (getString(R.string.student_past_sessions).equals(tabString)) {
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
        if (tabString.equals(getString(R.string.student_pending_sessions))) {
            return SessionStatus.PENDING;
        }
        if (tabString.equals(getString(R.string.rejected_sessions))) {
            return SessionStatus.REJECTED;
        }
        if (tabString.equals(getString(R.string.student_past_sessions))) {
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
        studentEmail = intent.getStringExtra("email");
        recycleView = findViewById(R.id.session_request_recycler_view);
        ula = new SessionListAdapter(new ArrayList<>(), true); // true indicates this is student view
        final TabLayout td = findViewById(R.id.student_tab_layout);
        myTabFilter = new TabFilter(td, ula);
        td.addOnTabSelectedListener(myTabFilter);
        filterSessionBy(SessionStatus.PENDING);
    }

    private void filterSessionBy(SessionStatus sessionStatus) {
        DatabaseReference sessionsReference = FirebaseDatabase.getInstance().getReference("sessions");
        Query studentSessions = sessionsReference.orderByChild("studentPhoneNumber").equalTo(studentPhoneNumber);

        studentSessions.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Session> sessionList = new ArrayList<>();

                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String requestStatusFromDatabase = ds.child("sessionStatus").getValue(String.class);
                        String studentPhoneNumberCheck = ds.child("studentPhoneNumber").getValue(String.class);

                        if (studentPhoneNumber.equals(studentPhoneNumberCheck) && sessionStatus.toString().equals(requestStatusFromDatabase)) {
                            Session s = ds.getValue(Session.class);
                            sessionList.add(s);
                        }
                    }
                }

                // Sort sessions in chronological order (most recent first)
                sortSessionsChronologically(sessionList);

                populateRecyclerView(sessionList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void sortSessionsChronologically(List<Session> sessions) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Collections.sort(sessions, new Comparator<Session>() {
            @Override
            public int compare(Session s1, Session s2) {
                try {
                    // Parse the date strings to Date objects
                    Date date1 = dateFormat.parse(s1.getDate());
                    Date date2 = dateFormat.parse(s2.getDate());

                    if (date1 != null && date2 != null) {
                        // Compare dates (most recent first)
                        int dateCompare = date1.compareTo(date2);

                        // If dates are the same, compare by start time
                        if (dateCompare == 0 && s1.getStartTime() != null && s2.getStartTime() != null) {
                            return s1.getStartTime().compareTo(s2.getStartTime());
                        }

                        return dateCompare;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // Fallback to string comparison if parsing fails
                return s1.getDate().compareTo(s2.getDate());
            }
        });
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

    public void onClickBackToWelcomePage(View view) {
        if (view.getId() == R.id.student_dashboard_back_button) {
            finish();
        }
    }

    public void onClickLogOff(View view) {
        if (view.getId() == R.id.log_off) {
            Intent intent = new Intent(StudentDashboardActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        TabLayout tb = findViewById(R.id.student_tab_layout);
        filterSessionBy(getSessionStatus(tb));
    }
}