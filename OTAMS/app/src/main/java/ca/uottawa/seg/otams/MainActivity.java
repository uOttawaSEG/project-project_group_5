package ca.uottawa.seg.otams;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.content.Intent;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
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


}