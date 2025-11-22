package ca.uottawa.seg.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

public class CourseSearchActivity extends AppCompatActivity {

    private String studentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_search);

        // Stores the email of the student that logged in that was passed from the previous activity
        Intent intent = getIntent();
        studentEmail = intent.getStringExtra("email");


        /*//KEEP THIS COMMENTED OUT BECAUSE IT BREAKS THE APP: when user clicks go to course search page, this piece of code makes it log out instead and you will never be able to access the actual page

        //search bar little bit of code
        SearchView courseSearchBar = findViewById(R.id.courseSearchBar);
        courseSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            //when user presses enter after inputting course code
            public boolean onQueryTextSubmit(String courseInput) {
                //put stuff here like getting the tutor info using the courseInput from search bar
                return true;
            }

            //when user is typing don't change the page or do anything
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        */

    }


    /*these are some ideas I was thinking about when creating the frontend. i put it in a comment just to explain what i did and hopefully guide the backend into what is going on - feel free to change it though!:

    visibility in frontend:- backend not yet implemented
    - when nothing is searched, no text or session list shows up
    - when searched, the courseResultsTitle and course_search_recycler_view ids in the activity_course_search xml file will show up. Right now they are hidden since nothing has been searched yet, but will need to change since currently android:visibility="gone"
    - i have courseResultsTitle as text that will appear after the search so the user knows what they searched for, in case they entered a new course code and didn't search it, they will at least know they are still stuck on previous search.
       -> i added 5 underscores as a blank area so that the course that was searched can be displayed there instead. not sure if we want this feature, and if so, it will need to be implemented in the backend i think

    search bar and searches: - backend not yet implemented
    - the search bar has light purple text that tells the user to enter a course code
    - once a user starts typing, the light-coloured prompt disappears and the actual input appears
    - user presses enter on keyboard to submit the input course code they entered
    - the code below is what happens after the enter key is pressed. this needs to be filled out with the backend logic of getting the data etc

    results and pulling from data: - backend not yet implemented
    - once the search enter is clicked, the results show up as recycle view
    - it shows tutor name, date, time, courses offered, rating, and a book button
    - the frontend of it is in activity_list_course_results. i will update the spacing of it later once i get an idea of how it looks when there is info
    - the info from the firebase database will show up in these fields: tutor name, date, time, courses

    average rating:- backend not sure if already implemented, ask Zahabia since she worked on rating front-end
    - not sure how rating will be calculated. check the rating page? will it be stored somewhere?

    book button: - backend not yet implemented
    - the book button will book the session and should then remove it from results - not yet implemented
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
