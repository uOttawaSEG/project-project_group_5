package ca.uottawa.seg.otams;

import android.app.Activity;
import android.view.View;

import androidx.appcompat.widget.SearchView;

public class CourseSearchActivity extends Activity {

    /*these are some hints I was thinking about when creating the frontend. i put it in a comment since it should probably be in onCreate()
    - the search bar has light purple text that tells the user to enter a course code
    - once a user starts typing, the light-coloured prompt disappears and the actual input appears
    - user presses enter on keyboard to submit the input course code they entered
    - the code below is what happens after the enter key is pressed. this needs to be filled out with the backend logic of getting the data etc


    //search bar
    SearchView courseSearchBar = findViewById(R.id.courseSearchBar);
    courseSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

        @Override
        //when user presses enter after inputting course code
        public boolean onQueryTextSubmit(String courseInput) {
            //put stuff here like getting the tutor info using the courseInput from search bar
            return true;
        }
    })
    */




    //return to dashboard button
    public void onClickBackToDashboard(View view) {
        int pressID=view.getId();

        // Check if the student is trying to return to their dashboard
        if (pressID == R.id.button_backToStudentDashboard) {
            // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
            finish();
        }
    }

}
