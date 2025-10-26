package ca.uottawa.seg.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class WelcomePageActivity extends AppCompatActivity {
    TextView role; // Represents the text field on the welcome page that shows the user's role

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        // Hook
        role = (TextView) findViewById(R.id.welcome_role_text);

        // Stores the role of the user that logged in that was passed from the previous activity
        Intent intent = getIntent();
        String userRole = intent.getStringExtra("role");

        role.setText(userRole); // Changes the role placeholder text to the user's actual role (displays the user's role)
    }

    public void onClickLogOff(View view) {
        int pressID=view.getId();

        // Check if the user is trying to log out
        if (pressID == R.id.button_log_off) {
            // Set the next page to the login page
            Intent intent = new Intent(WelcomePageActivity.this, MainActivity.class);

            // Send the user to the login page
            startActivity(intent);
        }
    }
}
