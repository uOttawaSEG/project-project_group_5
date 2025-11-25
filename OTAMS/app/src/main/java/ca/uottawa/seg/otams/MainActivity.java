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
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

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

        // Grab the data for all users in the database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query searchForUsers = reference.orderByChild("email").equalTo(usernameInput);

        searchForUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // If a user exists in the database with the entered info
                if(snapshot.exists()) {
                    username.setError(null);
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

                            if ("Administrator".equals(roleFromDatabase)) {
                                // If the user is an administrator then send them to a unique welcome page
                                Intent intent = new Intent(MainActivity.this, WelcomeAdminPageActivity.class);
                                // Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                                startActivity(intent);

                            } else {
                                String statusFromDB = userSnapshot.child("requestStatus").getValue(String.class);
                                RegistrationStatus rs = RegistrationStatus.valueOf(Objects.requireNonNull(statusFromDB));
                                if (rs.getStatusClass() != null) {
                                    Intent intent = new Intent(MainActivity.this, rs.getStatusClass());
                                    intent.putExtra("role", roleFromDatabase);

                                    startActivity(intent);
                                } else {
                                    if ("Student".equals(roleFromDatabase)) {
                                        Intent intent = new Intent(MainActivity.this, WelcomeStudentPageActivity.class);

                                        // Pass the user's role to the next page so it can be displayed there
                                        intent.putExtra("role", roleFromDatabase);
                                        intent.putExtra("email", userSnapshot.child("email").getValue(String.class));

                                        intent.putExtra("name", userSnapshot.child("firstName").getValue(String.class) + " " + userSnapshot.child("lastName").getValue(String.class));
                                        intent.putExtra("phoneNumber", userSnapshot.child("phoneNumber").getValue(String.class));

                                        // Send the user to the appropriate page
                                        startActivity(intent);
                                    }
                                    if ("Tutor".equals(roleFromDatabase)) {
                                        Intent intent = new Intent(MainActivity.this, WelcomeTutorPageActivity.class);
                                        intent.putExtra("phoneNumber", userSnapshot.child("phoneNumber").getValue(String.class));
                                        intent.putExtra("email", userSnapshot.child("email").getValue(String.class));
                                        startActivity(intent);
                                    }
                                }
                            }

                        } else {
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