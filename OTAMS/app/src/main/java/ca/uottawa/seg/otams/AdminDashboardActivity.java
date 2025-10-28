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
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private RegistrationStatus rs = null;
    private RecyclerView recycleView;
    private UserListAdapter ula;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.back_button), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recycleView = findViewById(R.id.request_recycler_view);
        final TabLayout tb = findViewById(R.id.admin_tab_layout);
        tb.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                RegistrationStatus tempRs = getRegistrationStatus(tab);
                // ula.updateData(filterUserDataBy(tempRs));
                filterUserDataBy(tempRs); // Changed because filterUserDataBy no longer returns the list
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
        // populateRecyclerView(filterUserDataBy(getRegistrationStatus(tb)));
        filterUserDataBy(getRegistrationStatus(tb)); // Changed because filterUserDataBy no longer returns the list
    }


    private RegistrationStatus getRegistrationStatus(TabLayout.Tab selectedTab) {
        String tabString = selectedTab.getText().toString();
        if (tabString.equals(getString(R.string.pending_requests))) {
            return RegistrationStatus.PENDING;
        }
        if (tabString.equals(getString(R.string.rejected_requests))) {
            return RegistrationStatus.REJECTED;
        }
        return RegistrationStatus.APPROVED;

    }
    private RegistrationStatus getRegistrationStatus(TabLayout tb) {
        TabLayout.Tab selectedTab = tb.getTabAt(tb.getSelectedTabPosition());
        if (selectedTab == null) {
            selectedTab = tb.getTabAt(0);
            selectedTab.select();
        }
        return getRegistrationStatus(selectedTab);
    }

    private void filterUserDataBy(RegistrationStatus registrationStatus) {
        /*
        List<User> userList = List.of(
                new Student("a", "b", "c", "e", "f", "g"),
                new Tutor("g", "h", "i", "j", "k", "l", "m")
        );
        return new ArrayList<>(userList);
        */

        // Grab all users' info from the database
        // DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("usersS");
        //**

        // Store all users listed in the database with the specified registration status
        Query searchForUsers = reference.orderByChild("requestStatus").equalTo(registrationStatus.toString());

        // Checks for changes to the database whenever returning to the dashboard
        searchForUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> userList = new ArrayList<>(); // List containing the details of every user who attempted to register (i.e. with request status of pending or rejected)

                // If a user exists in the database with the specified registration status
                if(snapshot.exists()) {

                    // Iterate through every user in the database with the specified registration status and adds it to the corresponding request inbox in the admin dashboard
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {

                        // Then fetch the request status of the user
                        String requestStatusFromDatabase = userSnapshot.child("requestStatus").getValue(String.class);

                        // If the expected registration status matches the request status of the user
                        if (requestStatusFromDatabase.equals(registrationStatus.toString())) {
                            // Then fetch the role the user wishes to register as
                            String roleFromDatabase = userSnapshot.child("role").getValue(String.class);

                            if (roleFromDatabase.equals("Student")) {
                                // If the request is to become a student then fill in the request details with the student's info
                                Student student = userSnapshot.getValue(Student.class);
                                userList.add(student); // Add the entry to the request list
                            }
                            else if (roleFromDatabase.equals("Tutor")) {
                                // If the request is to become a tutor then fill in the request details with the tutor's info
                                Tutor tutor = userSnapshot.getValue(Tutor.class);
                                userList.add(tutor); // Add the entry to the request list
                            }
                        }
                            /*
                            // Then fetch all of the relevant details of that user's request from the database
                            String roleFromDatabase = userSnapshot.child("role").getValue(String.class);
                            String firstNameFromDatabase = userSnapshot.child("firstName").getValue(String.class);
                            String lastNameFromDatabase = userSnapshot.child("lastName").getValue(String.class);
                            String emailFromDatabase = userSnapshot.child("email").getValue(String.class);
                            String phoneNumberFromDatabase = userSnapshot.child("phoneNumber").getValue(String.class);

                            // Fetch additional information depending on whether the request is to become a student or tutor
                            if ("Student".equals(roleFromDatabase)) {
                                String programFromDatabase = userSnapshot.child("program").getValue(String.class);

                                // Create a student entry with the necessary information from the database
                                Student student = new Student(firstNameFromDatabase, lastNameFromDatabase, emailFromDatabase, "", phoneNumberFromDatabase, programFromDatabase);
                                userList.add(student); // Add the entry to the request list
                            } else if ("Tutor".equals(roleFromDatabase)) {
                                String highestDegreeFromDatabase = userSnapshot.child("highestDegree").getValue(String.class);
                                String coursesOfferedFromDatabase = userSnapshot.child("coursesOffered").getValue(String.class);

                                // Create a tutor entry with the necessary information from the database
                                Tutor tutor = new Tutor(firstNameFromDatabase, lastNameFromDatabase, emailFromDatabase, "", phoneNumberFromDatabase, highestDegreeFromDatabase, coursesOfferedFromDatabase);
                                userList.add(tutor); // Add the entry to the request list
                            }
                        }
                        */
                    }
                }

                populateRecyclerView(userList); // Update the requests listed in the administrator's dashboard
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void populateRecyclerView(List<User> usersList) {
        if (ula == null) {
            // If the inbox is being populated for the first time then create and adapter for it
            RecyclerView rv = this.recycleView;
            rv.setLayoutManager(new LinearLayoutManager(this));
            ula = new UserListAdapter(usersList);
            rv.setAdapter(ula);
        } else {
            // If the adapter already exists then just update it instead of creating a new one
            ula.updateData(usersList);
        }
    }

    public void onClickViewRequestDetails(View view) {
        int pressID=view.getId();

        // Check if the user is trying to view a request's details
        if (pressID == R.id.arrow_text) {
            // If they are, then send the viewer to the page with the respective request's details
            Intent intent = new Intent(AdminDashboardActivity.this, AdminRequestDetailsActivity.class);

            // Send the user to the welcome page
            startActivity(intent);
        }
    }
    public void onClickLogOff(View view) {
        int pressID = view.getId();

        // Check if the user is trying to log out
        if (pressID == R.id.admin_log_off) {
            // Set the next page to the login page
            Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);

            // Send the user to the login page
            startActivity(intent);
        }
    }

    // Refresh the RecyclerView (request inbox) whenever the administrator returns back to the dashboard
    @Override
    protected void onResume() {
        super.onResume();

        // Determines which tab (Pending Requests or Rejected Requests) the administrator is currently on in the dashboard
        TabLayout tb = findViewById(R.id.admin_tab_layout);

        // Refreshes the request inbox for the selected tab
        filterUserDataBy(getRegistrationStatus(tb));
    }
}
