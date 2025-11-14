package ca.uottawa.seg.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
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

import android.widget.Switch;



public class ManageAvailabilityActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private final AtomicBoolean isUpdating = new AtomicBoolean(false);
    String tutorPhoneNumber; // Stores the phone number of the tutor so their entry in the database can quickly be found (since phone number is the key)

    private boolean automatic = false;

    private static Calendar getMidnightCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    private static final List<Date> ALL_TIME_SLOTS;
    static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
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
        // void onSuccess();
        void onSuccess(String message);
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
                // Date startTime = tsocl.getStartTime();
                // Date endTime = tsocl.getEndTime();

                Date rawStartTime = tsocl.getStartTime();
                Date rawEndTime   = tsocl.getEndTime();

                Date startTime = combineDateAndTime(c, rawStartTime);
                Date endTime   = combineDateAndTime(c, rawEndTime);

                addSlotInDatabase(c, startTime, endTime, new Callback() {
                    @Override
                    public void onSuccess(String message) {
                        // Toast.makeText(ManageAvailabilityActivity.this, "Added slots successfully!", Toast.LENGTH_LONG).show();

                        Toast.makeText(ManageAvailabilityActivity.this, message, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (e != null) {
                            Toast.makeText(ManageAvailabilityActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ManageAvailabilityActivity.this, "Failed to add timeslots.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        SwitchCompat approvalSwitch = findViewById(R.id.approvalSwitch);
        approvalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                automatic = isChecked;
            }
        });
    }

    private void addSlotInDatabase(Calendar dateFromCalendar, Date startTime, Date endTime, Callback c) {
        if (startTime == null || endTime == null) {
            c.onFailure(null);
            return;
        }

        // Determine the date that the tutor is attempting to create the timeslot on
        String sessionDate = dateFromCalendar.get(Calendar.DAY_OF_MONTH) + "/" + (dateFromCalendar.get(Calendar.MONTH) + 1) + "/" + dateFromCalendar.get(Calendar.YEAR);

        // Fetch tutor info and create slots selectively
        DatabaseReference tutor = FirebaseDatabase.getInstance().getReference("users").child(tutorPhoneNumber);
        tutor.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    c.onFailure(new Exception("Tutor not found in database."));
                    return;
                }

                // If the tutor exists within the database, saves their full name (the one stored in the database)
                String tutorFullName = snapshot.child("firstName").getValue(String.class) + " " + snapshot.child("lastName").getValue(String.class);

                // Split the timeslot into multiple smaller 30 minute timeslots
                List<Date[]> splitTimeslots = splitIntoThirtyIncSessions(startTime, endTime);

                DatabaseReference sessions = FirebaseDatabase.getInstance().getReference("sessions");

                // Find all sessions that the tutor currently attempting to create a timeslot has already created
                Query searchForTutorSessions = sessions.orderByChild("tutorPhoneNumber").equalTo(tutorPhoneNumber);

                searchForTutorSessions.addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        // Create a list containing all the sessions the tutor has previously created that may overlap with the
                        // session they are currently trying to create
                        List<Session> overlapSessions = new ArrayList<>();

                        // Iterate through every timeslot in the database that the tutor has already created
                        for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {

                            // Fetch all information about the session
                            Session tutorSessionInfo = sessionSnapshot.getValue(Session.class);

                            // Then fetch the date of the timeslot
                            String tutorSessionDate = tutorSessionInfo.getDate();

                            // If the previously created session found in the database is on the same day as the one the tutor
                            // is currently trying to create then add it to the list to be further investigated for overlap
                            if (sessionDate.equals(tutorSessionDate)) {
                                overlapSessions.add(tutorSessionInfo);
                            }
                        }

                        DatabaseReference databaseSessions = FirebaseDatabase.getInstance().getReference("sessions");

                        // Keeps track of the number of session slots created
                        int sessionSlotsCreated = 0;

                        // Determines which errors to show if any
                        boolean inPastError = false;
                        boolean overlapError = false;

                        // Iterate through the smaller 30 minute increment timeslots
                        for (Date[] createdTimeslot : splitTimeslots) {
                            // Determine the start and end time of the 30 minute timeslot
                            Date startSplitTimeslot = createdTimeslot[0];
                            Date endSplitTimeslot = createdTimeslot[1];

                            // Validate that the timeslot is not before the current time today
                            Calendar now = Calendar.getInstance();
                            Calendar splitStartCal = Calendar.getInstance();
                            splitStartCal.setTime(startSplitTimeslot);

                            boolean inPast = dateFromCalendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                                    dateFromCalendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) &&
                                    startSplitTimeslot.before(now.getTime());

                            boolean overlapExists = false;
                            for (Session s : overlapSessions) {
                                // Then fetch the start and end time of the timeslots the tutor has previously created that may overlap with the one currently being created
                                Date tutorSessionStartTime = s.getStartTime();
                                Date tutorSessionEndTime = s.getEndTime();

                                // Check if there's overlap (sessions that were rejected can be overlapped since they no longer exist for the tutor)
                                if (startSplitTimeslot.before(tutorSessionEndTime) && endSplitTimeslot.after(tutorSessionStartTime) && !s.getSessionStatus().equals("REJECTED")) {
                                    overlapExists = true;
                                    overlapError = true;
                                    break;
                                }
                            }

                            // If no overlap exists and the timeslot is not created for a time earlier than now then add the 30 minute timeslot to the database
                            if (!overlapExists && !inPast) {

                                // Give it a unique ID that will represent its key in the database
                                String id = databaseSessions.push().getKey();

                                Session newSession = new Session(id, sessionDate, startSplitTimeslot, endSplitTimeslot, tutorFullName, tutorPhoneNumber, "Khalid Riegan", "8888888888", SessionStatus.OPEN, false);

                                // If automatic approval is selected than the timeslot will immediately be approved when booked by a student
                                if (automatic) {
                                    newSession.setAutoApprove(true);
                                }

                                // Save the session as an entry in the database
                                databaseSessions.child(id).setValue(newSession);

                                sessionSlotsCreated++;
                            }

                            // If the timeslot was invalid then note why it was invalid (either overlaps with existing timeslots or was created for a time earlier than now)
                            if (overlapExists) {
                                overlapError = true;
                            }
                            if (inPast) {
                                inPastError = true;
                            }
                        }

                        if (sessionSlotsCreated == 1 && splitTimeslots.size() == 1) {
                            c.onSuccess("Timeslot created successfully!");
                        }
                        else if (sessionSlotsCreated == splitTimeslots.size()) {
                            c.onSuccess("All timeslots created successfully!");
                        }
                        else if (sessionSlotsCreated == 0 && overlapError && inPastError) {
                            c.onFailure(new Exception("ERROR: Cannot create timeslots that overlap or are earlier than now."));
                        }
                        else if (sessionSlotsCreated == 0 && overlapError) {
                            c.onFailure(new Exception("ERROR: Cannot create overlapping timeslots."));
                        }
                        else if (sessionSlotsCreated == 0 && inPastError) {
                            c.onFailure(new Exception("ERROR: Cannot create timeslots at a time earlier than now."));
                        }
                        else if (sessionSlotsCreated > 0 && overlapError && inPastError) {
                            c.onFailure(new Exception("WARNING: Some timeslots could not be created because of overlap or being for a time earlier than now."));
                        }
                        else if (sessionSlotsCreated > 0 && overlapError) {
                            c.onFailure(new Exception("WARNING: Some timeslots could not be created because they overlap with existing ones."));
                        }
                        else if (sessionSlotsCreated > 0 && inPastError) {
                            c.onFailure(new Exception("WARNING: Some timeslots could not be created because they are for a time earlier than now."));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private List<Date[]> splitIntoThirtyIncSessions(Date startTime, Date endTime) {
        // Store each date as two values in a list: start and end time
        List<Date[]> slots = new ArrayList<>();

        // Convert date objects into calender objects
        Calendar start = Calendar.getInstance();
        start.setTime(startTime);
        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        // Keep breaking up the large session into smaller 30 minute sessions until the session has fully been broked down
        while (start.before(end)) {
            // Clone the object so its start and end times can be modified without messing up the original
            Calendar next = (Calendar) start.clone();
            next.add(Calendar.MINUTE, 30);

            // Creates two date objects (start and end time) that represent one timeslot together
            slots.add(new Date[]{start.getTime(), next.getTime()});

            // Continue splitting the timeslot
            start = next;
        }

        return slots;
    }

    private Date combineDateAndTime(Calendar dateFromCalendar, Date timeOnly) {
        // Create a calender object with the date set to today (to be overridden with the session date later)
        Calendar sessionDate = Calendar.getInstance();

        // Set the time fields of the object (hours/minutes/seconds)
        sessionDate.setTime(timeOnly);

        // Overwrite the date fields with the date the tutor selected from the calender
        sessionDate.set(Calendar.YEAR, dateFromCalendar.get(Calendar.YEAR));
        sessionDate.set(Calendar.MONTH, dateFromCalendar.get(Calendar.MONTH));
        sessionDate.set(Calendar.DAY_OF_MONTH, dateFromCalendar.get(Calendar.DAY_OF_MONTH));

        return sessionDate.getTime();
    }

    public void onClickBackToDashboard(View view) {
        int pressID = view.getId();

        // Check if the tutor is trying to return back to their dashboard
        if (pressID == R.id.backToDashboardBtn) {
            finish(); // Remove the current activity from the activity stack (go back to the previous activity i.e. the dashboard)
        }
    }
}