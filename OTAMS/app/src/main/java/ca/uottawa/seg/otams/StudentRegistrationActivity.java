package ca.uottawa.seg.otams;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



public class StudentRegistrationActivity extends AppCompatActivity {

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
        }else if(numberInput.matches("\\d{10}")) {//check if the number matches 10 digits
            number.setError("Phone number must be 10 digits");
            valid = false;
        }
        if(programInput.isEmpty()){//check if their input is empty
            program.setError("Program of study is required");
            valid=false;
        }

        if(valid==true){//check if all fields have been filled in with appropriate infomratio
            setContentView(R.layout.activity_welcome_page);//redirect to welcome page

        }



    }

}