package ca.uottawa.seg.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class ManageAvailabilityActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private AtomicBoolean isUpdating = new AtomicBoolean(false);
    String tutorPhoneNumber; // Stores the phone number of the tutor so their entry in the database can quickly be found (since phone number is the key)

    private static Calendar getMidnightCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    private static final List<Date> ALL_TIME_SLOTS;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    static {
        List<Date> timeSlots = new ArrayList<>();
        Calendar calendar = getMidnightCalendar();
        while (calendar.get(Calendar.HOUR_OF_DAY) < 23) {
            timeSlots.add(calendar.getTime());
            calendar.add(Calendar.MINUTE, 30);
        }
        ALL_TIME_SLOTS = Collections.unmodifiableList(timeSlots);
    }

    public interface Callback {
        void onSuccess();
        void onFailure(Exception e);
    }


    private static class DateChangeListener implements CalendarView.OnDateChangeListener {

        private final Calendar currentSetCalendar;
        private DateChangeListener(Calendar c) {
            currentSetCalendar = c;
        }
        @Override
        public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
            currentSetCalendar.set(Calendar.YEAR, year);
            currentSetCalendar.set(Calendar.MONTH, month);
            currentSetCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        }

        public Calendar getCurrentSetCalendar() {
            return currentSetCalendar;
        }
    }

    private class TimeSlotOnClickListener implements AdapterView.OnItemSelectedListener {

        private final Spinner startSpinner;
        private final Spinner endSpinner;

        private int startPosition, endPosition;

        private TimeSlotOnClickListener(Spinner startSpin, Spinner endSpin) {
            startSpinner = startSpin;
            endSpinner = endSpin;
            startPosition = 0;
            endPosition = 1;
        }

        public void addListeners() {
            startSpinner.setOnItemSelectedListener(this);
            endSpinner.setOnItemSelectedListener(this);
        }

        public void setAdapters() {
            List<String> timesList = ALL_TIME_SLOTS.stream().map(sdf::format).collect(Collectors.toList());
            ArrayAdapter<String> startTimeAdapter = new ArrayAdapter<>(ManageAvailabilityActivity.this,
                    android.R.layout.simple_spinner_dropdown_item,
                    timesList);
            ArrayAdapter<String> endTimeAdapter = new ArrayAdapter<>(ManageAvailabilityActivity.this,
                    android.R.layout.simple_spinner_dropdown_item,
                    timesList);
            startSpinner.setAdapter(startTimeAdapter);
            endSpinner.setAdapter(endTimeAdapter);
            startSpinner.setSelection(startPosition);
            endSpinner.setSelection(endPosition);
        }

        public Date getStartTime() {
            return this.startPosition < this.endPosition ? ALL_TIME_SLOTS.get(this.startPosition) : null;
        }

        public Date getEndTime() {
            return this.endPosition > startPosition ? ALL_TIME_SLOTS.get(this.endPosition) : null;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (isUpdating.get()) return;
            isUpdating.set(true);
            if (parent.getId() == startSpinner.getId()) {
                startPosition = position;
            }
            if (parent.getId() == endSpinner.getId()) {
                endPosition = position;
            }
            if (endPosition <= startPosition) {
                Toast.makeText(
                        ManageAvailabilityActivity.this,
                        "End time cannot be earlier or the same as start time",
                        Toast.LENGTH_SHORT).show();
                startSpinner.setOnItemSelectedListener(null);
                endSpinner.setOnItemSelectedListener(null);
                try {
                    if (endPosition <= 1) {
                        endPosition = startPosition + 1;
                    } else {
                        startPosition = endPosition - 1;
                    }
                    if (startPosition <= 0) {
                            startPosition = 0;
                            endPosition = 1;
                    }
                    endSpinner.setSelection(endPosition);
                    startSpinner.setSelection(startPosition);
                } finally {
                    endSpinner.setOnItemSelectedListener(this);
                    startSpinner.setOnItemSelectedListener(this);
                }
            }
            isUpdating.set(false);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_availability);

        // Stores the phone number of the tutor that was passed from the previous activity (used when creating a session object)
        Intent intent = getIntent();
        tutorPhoneNumber = intent.getStringExtra("phoneNumber");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.back_button), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Calendar calendar = getMidnightCalendar();
        final DateChangeListener dcl = new DateChangeListener(calendar);
        calendarView = findViewById(R.id.calendar_view);
        calendarView.setMinDate(calendar.getTimeInMillis());
        calendarView.setDate(calendar.getTimeInMillis());
        //sets date to current time
        calendarView.setOnDateChangeListener(dcl);
        final TimeSlotOnClickListener tsocl = new TimeSlotOnClickListener(
                findViewById(R.id.start_time_spinner),
                findViewById(R.id.end_time_spinner)
        );
        tsocl.setAdapters();
        tsocl.addListeners();
        Button setAvailabilityButton = findViewById(R.id.manage_set_availability);


        setAvailabilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = dcl.getCurrentSetCalendar();
                Date startTime = tsocl.getStartTime();
                Date endTime = tsocl.getEndTime();
                addSlotInDatabase(c, startTime, endTime, new Callback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ManageAvailabilityActivity.this, "Added slot successfully!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (e != null) {
                            Toast.makeText(ManageAvailabilityActivity.this, "Failed to add slot: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ManageAvailabilityActivity.this, "Failed to add slot.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


    }

    private void addSlotInDatabase(Calendar dateFromCalendar, Date startTime, Date endTime, Callback c) {
        if (startTime == null || endTime == null) {
            c.onFailure(null);
            return;
        }

        // Determine the date that the tutor is attempting to create the timeslot on
        String sessionDate = dateFromCalendar.get(Calendar.DAY_OF_MONTH) + "/" + dateFromCalendar.get(Calendar.MONTH) + "/" + dateFromCalendar.get(Calendar.YEAR);

        // Check if the timeslot overlaps with any of the tutor's previously created ones
        checkForOverlap(sessionDate, startTime, endTime, new Callback() {
            @Override
            public void onSuccess() {

                // If it does not, then find the tutor attempting to create a timeslot inside the database
                DatabaseReference tutor = FirebaseDatabase.getInstance().getReference("users").child(tutorPhoneNumber);

                tutor.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // If the tutor exists within the database, saves their full name (the one stored in the database)
                            String tutorFullName = snapshot.child("firstName").getValue(String.class) + " " + snapshot.child("lastName").getValue(String.class);

                            // Create a session to represent the timeslot the tutor is trying create
                            DatabaseReference databaseSessions = FirebaseDatabase.getInstance().getReference("sessions");

                            // Give it a unique ID that will represent its key in the database
                            String id = databaseSessions.push().getKey();

                            Session session = new Session(id, sessionDate, startTime, endTime, tutorFullName, tutorPhoneNumber, null, null);

                            // Save the session as an entry in the database
                            databaseSessions.child(id).setValue(session);

                            // Alert the tutor that the timeslot was successfully created
                            c.onSuccess();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                // Alert the tutor if the timeslot overlaps with one of their previously created ones
                c.onFailure(new Exception("Timeslot overlaps with an existing one."));
            }
        });
    }

    private void checkForOverlap(String date, Date startTime, Date endTime, Callback c) {

        DatabaseReference sessions = FirebaseDatabase.getInstance().getReference("sessions");

        // Find all sessions that the tutor currently attempting to create a timeslot has already created
        Query searchForTutorSessions = sessions.orderByChild("tutorPhoneNumber").equalTo(tutorPhoneNumber);

        searchForTutorSessions.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Iterate through every timeslot in the database that the tutor has already created
                    for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {

                        Session tutorSession = sessionSnapshot.getValue(Session.class);

                        // Then fetch the time and date of the timeslot the tutor previously created
                        Date tutorSessionStartTime = tutorSession.getStartTime();
                        Date tutorSessionEndTime = tutorSession.getEndTime();
                        String tutorSessionDate = tutorSession.getDate();

                        // if (startTime.before(tutorSessionEndTime.getTime()) && endTime.after(tutorSessionStartTime.getTime())) {
                        if (date.equals(tutorSessionDate) && startTime.before(tutorSessionEndTime) && endTime.after(tutorSessionStartTime)) {
                            // If the timeslot the tutor is trying to create overlaps with a timeslot they have already created then alert the user and do not create the timeslot
                            c.onFailure(new Exception("Timeslot overlaps with an existing one."));
                            return; // Do not check any further for overlapping timeslots once one has been found
                        }
                    }

                }
                // If no overlapping timeslots were found then alert the tutor that their timeslot was successfully created
                c.onSuccess();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    public void onClickBack(View view) {
        int pressID=view.getId();

        // Check if the user is trying to log out
        if (pressID == R.id.button_log_off) {
            // Set the next page to the login page
            Intent intent = new Intent(ManageAvailabilityActivity.this, MainActivity.class);

            // Send the user to the login page
            startActivity(intent);
        }
    }
}
