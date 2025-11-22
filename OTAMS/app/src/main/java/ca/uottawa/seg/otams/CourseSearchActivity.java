package ca.uottawa.seg.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

public class CourseSearchActivity extends AppCompatActivity {

    private String studentEmail;
    private String studentPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_search);

        // Received from previous activity (may be null)
        Intent intent = getIntent();
        studentEmail = intent.getStringExtra("email");
        studentPhoneNumber = intent.getStringExtra("phoneNumber");

        SearchView courseSearchBar = findViewById(R.id.courseSearchBar);
        // Make sure submit button is enabled
        courseSearchBar.setSubmitButtonEnabled(true);

        courseSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Called when user presses enter or the submit (magnifier) icon
                if (query == null || query.trim().isEmpty()) {
                    Toast.makeText(CourseSearchActivity.this, "Please enter a course code", Toast.LENGTH_SHORT).show();
                    return true;
                }

                // Start results activity, pass the query and current student info
                Intent resultsIntent = new Intent(CourseSearchActivity.this, CourseSearchResultsActivity.class);
                resultsIntent.putExtra("courseQuery", query.trim());
                resultsIntent.putExtra("email", studentEmail);
                resultsIntent.putExtra("phoneNumber", studentPhoneNumber);
                startActivity(resultsIntent);

                // Collapse keyboard / SearchView default behavior
                courseSearchBar.onActionViewCollapsed();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // do nothing for now
                return false;
            }
        });

        // Optional: if user clicks magnifier when empty, we show a hint
        courseSearchBar.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // nothing
            }
        });
    }

    //return to dashboard button
    public void onClickBackToDashboard(android.view.View view) {
        if (view.getId() == R.id.button_backToStudentDashboard) {
            finish();
        }
    }
}
