package ca.uottawa.seg.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ActivityAvailibilitySlots extends AppCompatActivity {
    private RecyclerView recycleView;
    private UserListAdapter ula;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_availability_slots);


    }

    public void onClickBackToDashboard(View view) {
        int pressID=view.getId();

        // Check if the user is trying to log out
        if (pressID == R.id.button_log_off) {
            // Set the next page to the login page
            Intent intent = new Intent(ActivityAvailibilitySlots.this, TutorDashboardActivity.class);

            // Send the user to the login page
            startActivity(intent);
        }
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
}
