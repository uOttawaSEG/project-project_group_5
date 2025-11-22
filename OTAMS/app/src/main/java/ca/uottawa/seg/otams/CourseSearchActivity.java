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
    private SearchView courseSearchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_search);

        // get extras passed from previous screen (may be null)
        Intent intent = getIntent();
        studentEmail = intent.getStringExtra("email");
        studentPhoneNumber = intent.getStringExtra("phoneNumber");

        courseSearchBar = findViewById(R.id.courseSearchBar);
        // show the submit (magnifier) button inside the SearchView
        courseSearchBar.setSubmitButtonEnabled(true);

        // optional: make IME action be "search"
        courseSearchBar.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        courseSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // trim and basic validation
                if (query == null) return true;
                String courseQuery = query.trim();
                if (courseQuery.isEmpty()) {
                    // give quick feedback
                    Toast.makeText(CourseSearchActivity.this, "Please enter a course code", Toast.LENGTH_SHORT).show();
                    return true; // handled
                }

                // Start the results activity and pass the search string (and student info)
                Intent resultsIntent = new Intent(CourseSearchActivity.this, MainActivity.class);
                // Intent resultsIntent = new Intent(CourseSearchActivity.this, CourseSearchResultsActivity.class);
                resultsIntent.putExtra("courseQuery", courseQuery);
                resultsIntent.putExtra("email", studentEmail);
                resultsIntent.putExtra("phoneNumber", studentPhoneNumber);
                startActivity(resultsIntent);

                // collapse keyboard and clear focus (SearchView specific)
                courseSearchBar.clearFocus();
                return true; // indicate we handled the submit
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // we don't react while typing for now
                return false;
            }
        });
    }

    // back button — unchanged
    public void onClickBackToDashboard(android.view.View view) {
        int pressID = view.getId();
        if (pressID == R.id.button_backToStudentDashboard) {
            finish();
        }
    }
}
