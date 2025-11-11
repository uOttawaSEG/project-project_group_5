package ca.uottawa.seg.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TutorDashboardActivity extends AppCompatActivity {

    //private RegistrationStatus rs = null;
    private RecyclerView recycleView;
    private SessionListAdapter ula;
    String tutorPhoneNumber; // Stores the phone number of the tutor so their entry in the database can quickly be found (since phone number is the key)

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

        recycleView = findViewById(R.id.session_request_recycler_view);
        final TabLayout td = findViewById(R.id.tutor_tab_layout);
        td.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tabString = Objects.requireNonNull(tab.getText()).toString();
                Calendar c = Calendar.getInstance();
                List<Session> allSessions = new ArrayList<>();
                String tutorEmail = getIntent().getStringExtra("email");
                if (getString(R.string.pending_session_requests).equals(tabString)) {
                    allSessions = filterSessionBy(tutorEmail, s -> RegistrationStatus.valueOf(s.getSessionStatus()) == RegistrationStatus.PENDING);
                }
                if (getString(R.string.upcoming_sessions).equals(tabString)) {
                    allSessions = filterSessionBy(tutorEmail, s -> s.getStartTime().after(c.getTime()));
                }
                if (getString(R.string.past_sessions).equals(tabString)) {
                    allSessions = filterSessionBy(tutorEmail, s -> s.getStartTime().before(c.getTime()));
                }
                populateRecyclerView(allSessions);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                ula.clearData();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab);
            }
        });
    }

    private List<Session> filterSessionBy(String tutorEmail, Predicate<Session> filterMechanism) {
            return getAllSessionsOfTutor(tutorEmail).stream().filter(filterMechanism).collect(Collectors.toList());
    }

    private List<Session> getAllSessionsOfTutor(String tutorEmail) {
        // Get all sessions from Firebase
        return new ArrayList<>(List.of());
    }

    private void populateRecyclerView(List<Session> sessionsList) {
        if (ula == null) {
            // If the inbox is being populated for the first time then create and adapter for it
            RecyclerView rv = this.recycleView;
            rv.setLayoutManager(new LinearLayoutManager(this));
            ula = new SessionListAdapter(sessionsList);
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
