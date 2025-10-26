package ca.uottawa.seg.otams;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

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
                ula.updateData(filterUserDataBy(tempRs));
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
        populateRecyclerView(filterUserDataBy(getRegistrationStatus(tb)));
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

    private List<User> filterUserDataBy(RegistrationStatus registrationStatus) {
        List<User> userList = List.of(
                new Student("a", "b", "c", "e", "f", "g"),
                new Tutor("g", "h", "i", "j", "k", "l", "m")
        );
        return new ArrayList<>(userList);
    }

    private void populateRecyclerView(List<User> usersList) {
        RecyclerView rv = this.recycleView;
        rv.setLayoutManager(new LinearLayoutManager(this));
        ula = new UserListAdapter(usersList);
        rv.setAdapter(ula);
    }

}
