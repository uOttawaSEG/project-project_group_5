package ca.uottawa.seg.otams;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.back_button), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onClickRegistration(View view){

        int pressID=view.getId();

        if(pressID==R.id.register_student_btn){
            Intent intent = new Intent(MainActivity.this, StudentRegistrationActivity.class);
            startActivity(intent);
        }

        if(pressID==R.id.register_tutor_btn){
            Intent intent = new Intent(MainActivity.this, TutorRegistrationActivity.class);
            startActivity(intent);
        }
    }

    public void onClickLogin(View view) {
        int pressID=view.getId();

        // Check if the user is trying to log in
        if (pressID == R.id.login_btn) {
            // Check if the user is registered in the database
            isRegistered();
        }
    }

    private void isRegistered() {
        EditText username = findViewById(R.id.username_input);
        EditText password = findViewById(R.id.password_input);

        String usernameInput = username.getText().toString().trim();
        String passwordInput = password.getText().toString().trim();

        // Check if any info is missing and tell the user to fill those fields out if so
        if (usernameInput.isEmpty()) {
            username.setError("Please fill out all fields");
            return;
        }
        if (passwordInput.isEmpty()) {
            password.setError("Please fill out all fields");
            return;
        }

        // Grab all users' info from the database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        // Store all users listed in the database with the same first name (username) was entered
        Query searchForUsers = reference.orderByChild("firstName").equalTo(usernameInput);

        searchForUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // If a user exists in the database with the entered info
                if(snapshot.exists()) {
                    username.setError(null);
                    // username.setErrorEnabled(false);

                    /*
                    Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot userSnapshot = iterator.next();
                    */

                    // Iterate through every user in the database with a matching username to the one entered
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {

                        // Then fetch the password of any user with the entered username
                        String passwordFromDatabase = userSnapshot.child("password").getValue(String.class);

                        // If the password entered matches the password stored under that user's entry
                        if (passwordFromDatabase.equals(passwordInput)) {
                            password.setError(null);
                            // password.setErrorEnabled(false);

                            // Then fetch the user's role from the database
                            String roleFromDatabase = userSnapshot.child("role").getValue(String.class);

                            if (Administrator.role.equals(roleFromDatabase)) {
                                // If the user is an administrator then send them the the admin dashboard
                                Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                                startActivity(intent);

                            } else {
                                // Set the next page as the welcome page
                                Intent intent = new Intent(MainActivity.this, WelcomePageActivity.class);

                                // Pass the user's role to the next page so it can be displayed there
                                intent.putExtra("role", roleFromDatabase);

                                // Send the user to the welcome page
                                startActivity(intent);
                            }
                        }
                        else {
                            // Tell the user if the password did not match
                            password.setError("Incorrect password.");
                            password.requestFocus();
                        }
                    }
                }
                else {
                    // If the user does not exist in the database then they have not yet registered
                    username.setError("No such user exists. Please register before attempting to login.");
                    username.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}