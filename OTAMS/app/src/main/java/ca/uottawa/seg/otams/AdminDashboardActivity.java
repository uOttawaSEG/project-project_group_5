package ca.uottawa.seg.otams;

import android.app.Activity;
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

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
                filterUserDataBy(tempRs); // Changed filterUserDataBy no longer returns the list
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
        filterUserDataBy(getRegistrationStatus(tb)); // Changed filterUserDataBy no longer returns the list
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

        // Grab all users info from database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        // Store all users listed in the database with the desired registration status
        Query searchForUsers = reference.orderByChild("requestStatus").equalTo(registrationStatus.toString());

        // searchForUsers.addListenerForSingleValueEvent(new ValueEventListener()

        // Checks for changes in real time
        searchForUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> userList = new ArrayList<>(); // List containing the user's data as individual entries

                // If a user exists in the database with the specified registration status
                if(snapshot.exists()) {

                    // Iterate through every user in the database with the specified registration status and add it to to request list in the admin dashboard
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {

                        // Then fetch the request status of any user with the desired registration status
                        String requestStatusFromDatabase = userSnapshot.child("requestStatus").getValue(String.class);

                        // If the registration status matches the one in that user's entry
                        if (requestStatusFromDatabase.equals(registrationStatus.toString())) {
                            // Then fetch the all of that user's info from the database
                            String roleFromDatabase = userSnapshot.child("role").getValue(String.class);
                            String firstNameFromDatabase = userSnapshot.child("firstName").getValue(String.class);
                            String lastNameFromDatabase = userSnapshot.child("lastName").getValue(String.class);
                            String emailFromDatabase = userSnapshot.child("email").getValue(String.class);
                            String phoneNumberFromDatabase = userSnapshot.child("phoneNumber").getValue(String.class);

                            // Fetch additional information depending on whether the user's request is from a student or tutor
                            if (roleFromDatabase.equals("Student")) {
                                String programFromDatabase = userSnapshot.child("program").getValue(String.class);

                                // Create a student entry with the necessary information from the database
                                Student student = new Student(firstNameFromDatabase, lastNameFromDatabase, emailFromDatabase, "", phoneNumberFromDatabase, programFromDatabase);
                                userList.add(student);
                            } else if (roleFromDatabase.equals("Tutor")) {
                                String highestDegreeFromDatabase = userSnapshot.child("highestDegree").getValue(String.class);
                                String coursesOfferedFromDatabase = userSnapshot.child("coursesOffered").getValue(String.class);

                                // Create a student entry with the necessary information from the database
                                Tutor tutor = new Tutor(firstNameFromDatabase, lastNameFromDatabase, emailFromDatabase, "", phoneNumberFromDatabase, highestDegreeFromDatabase, coursesOfferedFromDatabase);
                                userList.add(tutor);
                            }
                        }
                    }
                }

                populateRecyclerView(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void populateRecyclerView(List<User> usersList) {
        RecyclerView rv = this.recycleView;
        rv.setLayoutManager(new LinearLayoutManager(this));
        ula = new UserListAdapter(usersList);
        rv.setAdapter(ula);
    }
}
