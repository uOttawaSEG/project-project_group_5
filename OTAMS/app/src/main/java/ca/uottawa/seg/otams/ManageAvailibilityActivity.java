package ca.uottawa.seg.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

public class ManageAvailibilityActivity extends AppCompatActivity {
    //private RegistrationStatus rs = null;
    private RecyclerView recycleView;
    private UserListAdapter ula;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_manage_availibility);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.back_button), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onClickLogOff(View view) {
        int pressID=view.getId();

        // Check if the user is trying to log out
        if (pressID == R.id.button_log_off) {
            // Set the next page to the login page
            Intent intent = new Intent(ManageAvailibilityActivity.this, MainActivity.class);

            // Send the user to the login page
            startActivity(intent);
        }
    }
}
