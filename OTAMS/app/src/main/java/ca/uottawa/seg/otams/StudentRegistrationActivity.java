package ca.uottawa.seg.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class StudentRegistrationActivity extends AppCompatActivity {

    // Variables to access realtime database
    FirebaseDatabase rootNode;
    DatabaseReference reference;

    EditText firstName;
    EditText lastName;
    EditText email;
    EditText password;
    EditText number;
    EditText program;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_student_registration);
        firstName=findViewById(R.id.student_first_name);
        lastName=findViewById(R.id.student_last_name);
        email=findViewById(R.id.student_email_address);
        password=findViewById(R.id.student_account_password);
        number=findViewById(R.id.student_phone_number);
        program=findViewById(R.id.student_program_of_study);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onClickStudentRegistrationButton(View view) {
        String firstNameInput=firstName.getText().toString();
        String lastNameInput=lastName.getText().toString();
        String emailInput=email.getText().toString();
        String passwordInput=password.getText().toString();
        String numberInput=number.getText().toString();
        String programInput=program.getText().toString();

        boolean valid=true;//initalize validity

        if(firstNameInput.isEmpty()){//check if their input is empty
            firstName.setError("First name is required");
            valid=false;
        }
        if(lastNameInput.isEmpty()){//check if their input is empty
            lastName.setError("Last name is required");
            valid=false;
        }
        if(emailInput.isEmpty()){//check if their input is empty
            email.setError("Email address is required");
            valid=false;
        }
        if(passwordInput.isEmpty()){//check if their input is empty
            password.setError("Password is required");
            valid=false;
        }else if(passwordInput.length()<6){//check if the password length is enoguh
            password.setError("Please enter a password 6 or more characters");
            valid=false;
        }
        if(numberInput.isEmpty()) {//check if their input is empty
            number.setError("Phone number is required");
            valid = false;
        }else if(numberInput.matches("\\d{9}")) { //check if the number matches 10 digits
            number.setError("Phone number must be 10 digits");
            valid = false;
        }
        if(programInput.isEmpty()){//check if their input is empty
            program.setError("Program of study is required");
            valid=false;
        }

        if(valid==true){//check if all fields have been filled in with appropriate infomratio

            // If all inputs are valid and filled, then store all
            // the information within the users tree of the database
            rootNode = FirebaseDatabase.getInstance();
            reference = rootNode.getReference("users");

            // Create an object with the entered information
            UserRegistrationInfoDatabase studentRegistration = new UserRegistrationInfoDatabase(firstNameInput, lastNameInput, emailInput, passwordInput, numberInput, programInput, "", "", "Student");

            // Create a new user entry for the newly registered student and assign their phone number as their key (since phone numbers are unique for each individual)
            reference.child(numberInput).setValue(studentRegistration);

            // setContentView(R.layout.activity_welcome_page);//redirect to welcome page

            // Set the next page as the welcome page
            Intent intent = new Intent(StudentRegistrationActivity.this, WelcomePageActivity.class);

            // Pass the user's role to the next page so it can be displayed there
            intent.putExtra("role", "Student");

            // Send the user to the welcome page
            startActivity(intent);
        }



    }

}