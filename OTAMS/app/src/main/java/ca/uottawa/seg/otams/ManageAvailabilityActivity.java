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
                            Toast.makeText(ManageAvailabilityActivity.this, "Failed to add slots: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ManageAvailabilityActivity.this, "Failed to add slots", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


    }

    private void addSlotInDatabase(Calendar date, Date startTime, Date endTime, Callback c) {
        if (startTime == null || endTime == null) {
            c.onFailure(null);
        }
    }

    public void onClickLogOff(View view) {
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
