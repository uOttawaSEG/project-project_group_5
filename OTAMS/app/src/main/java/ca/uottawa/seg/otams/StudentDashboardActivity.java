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
import java.util.Calendar;
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
            Calendar c = Calendar.getInstance();
            Date now = c.getTime();
            filter=null;
            //String tabString = Objects.requireNonNull(tab.getText()).toString();
            SessionStatus sessionStatus = SessionStatus.PENDING;
            if (getString(R.string.student_upcoming_sessions).equals(tabString)) {
                sessionStatus = SessionStatus.APPROVED;
            } else if (getString(R.string.student_past_sessions).equals(tabString)) {
                sessionStatus = SessionStatus.COMPLETED;
                //filter = s -> s.getEndTime().after(now);
                filter = s -> s.getEndTime() != null && s.getEndTime().before(now);
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
                    Calendar c = Calendar.getInstance();
                    Date now = c.getTime();

                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String requestStatusFromDatabase = ds.child("sessionStatus").getValue(String.class);
                        String studentPhoneNumberCheck = ds.child("studentPhoneNumber").getValue(String.class);

                        if (studentPhoneNumber.equals(studentPhoneNumberCheck)) {
                            Session s = ds.getValue(Session.class);

                            if (s != null) {
                                // Reconstruct the start/end times from Firebase
                                Date startTime = reconstructSessionDate(ds.child("startTime"));
                                Date endTime = reconstructSessionDate(ds.child("endTime"));
                                s.setStartTime(startTime);
                                s.setEndTime(endTime);

                                // Handle sessions that have already passed
                                if (endTime != null && endTime.before(now)) {
                                    if ("PENDING".equals(requestStatusFromDatabase)) {
                                        // Delete session requests that are still pending and have already passed from the database
                                        ds.getRef().removeValue();
                                        continue; // Skips adding this session to the list
                                    } else if ("APPROVED".equals(requestStatusFromDatabase)) {
                                        // Update the status of approved sessions that have passed to completed to indicate that they should go in the past session tab
                                        s.setSessionStatus(SessionStatus.COMPLETED);
                                        ds.getRef().child("sessionStatus").setValue("COMPLETED");
                                        requestStatusFromDatabase = "COMPLETED";
                                    }
                                }

                                // Only add sessions that match the current tab's filter
                                if (sessionStatus.toString().equals(requestStatusFromDatabase)) {
                                    sessionList.add(s);
                                }
                            }
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
    private Date reconstructSessionDate (DataSnapshot snapshot) {
        if (snapshot.exists()) {
            // Fetch the values of each part of the start or end time in the database
            Integer date = snapshot.child("date").getValue(Integer.class);
            Integer year = snapshot.child("year").getValue(Integer.class);
            Integer month = snapshot.child("month").getValue(Integer.class); // 0-based
            // Integer day = snapshot.child("day").getValue(Integer.class);
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

    @Override
    protected void onResume() {
        super.onResume();
        TabLayout tb = findViewById(R.id.student_tab_layout);
        filterSessionBy(getSessionStatus(tb));
    }
}