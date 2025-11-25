package ca.uottawa.seg.otams;

import static ca.uottawa.seg.otams.ManageAvailabilityActivity.sdf;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseListAdapter extends RecyclerView.Adapter<CourseListView> {

    // Implemented to allow the adapter to notify the course search activity when a session is booked
    public interface OnSessionBookedListener {
        void onSessionBooked(Session session);
    }

    private static class RequestSessionButtonListener implements View.OnClickListener {

        private final Button buttonRef;
        private final Session session;
        private final String studentPhoneNumber;
        private final String studentName;
        private final CourseSearchActivity activity;
        private final OnSessionBookedListener callback;

        private RequestSessionButtonListener(Button btn, Session s, String phoneNumber, String name, CourseSearchActivity activity, OnSessionBookedListener callback) {
            this.buttonRef = btn;
            this.session = s;
            this.studentPhoneNumber = phoneNumber;
            this.studentName = name;
            this.activity = activity;
            this.callback = callback;
        }

        @Override
        public void onClick(View v) {

            // When the student clicks the button to book a session, checks if the session overlaps with one of their existing sessions
            if (activity != null && activity.hasTimeConflict(session.getStartTime(), session.getEndTime())) {
                // If there is an overlap then alert the student
                Toast.makeText(this.buttonRef.getContext(), "ERROR: A scheduling conflict exists with another booked session", Toast.LENGTH_SHORT).show();
                return;
            }

            // Only allows the button to be clicked once (the student cannot try to book the session multiple times)
            this.buttonRef.setEnabled(false);

            DatabaseReference sessions = FirebaseDatabase.getInstance().getReference("sessions");
            String sessionId = this.session.getId();

            String sessionStatus = SessionStatus.PENDING.toString();

            // Determine whether the tutor enabled auto approval for that session slot and sets the session's status to approved instead of pending if it is
            if (this.session.getAutoApprove()) {
                sessionStatus = SessionStatus.APPROVED.toString();
            }

            // Create a map with the data that needs to be added to the sessionentry in the database (i.e. need to update the session's status and include some info regarding the student who booked it)
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("sessionStatus", sessionStatus);
            sessionData.put("studentPhoneNumber", this.studentPhoneNumber);
            sessionData.put("studentName", this.studentName);

            sessions.child(sessionId).updateChildren(sessionData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Updates the session entry in the database with the new info

                    // Alerts the student that their booking was successful (different message depending on whether the request was approved immediately (i.e. auto approval was on) or not)
                    if (this.session.getAutoApprove()) {
                        this.session.setSessionStatus(SessionStatus.APPROVED);

                        Toast.makeText(this.buttonRef.getContext(), "Session booked!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        this.session.setSessionStatus(SessionStatus.PENDING);

                        Toast.makeText(this.buttonRef.getContext(), "Session requested!", Toast.LENGTH_SHORT).show();
                    }

                    this.session.setStudentPhoneNumber(this.studentPhoneNumber);
                    this.session.setStudentName(this.studentName);

                    // Notifies the course search activity that the session was successfully booked
                    if (callback != null) {
                        callback.onSessionBooked(this.session);
                    }
                } else {
                    // If the session was unable to be booked then alert the student
                    Toast.makeText(this.buttonRef.getContext(), "ERROR: Failed to request session: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    // Allow the button to be pressed again if it failed to do anything the first time
                    this.buttonRef.setEnabled(true);
                }
            });
        }
    }

    private final List<Session> courseSession;
    private final Map<String, Tutor> tutorMap;
    private final String studentPhoneNumber;
    private final String studentName;
    private final CourseSearchActivity activity;
    private final OnSessionBookedListener onSessionBookedListener;

    public CourseListAdapter(Map<String, Tutor> tutorMap, String studentPhoneNumber, String studentName, CourseSearchActivity activity, OnSessionBookedListener listener) {
        this.courseSession = new ArrayList<>();
        this.tutorMap = tutorMap;
        this.studentPhoneNumber = studentPhoneNumber;
        this.studentName = studentName;
        this.activity = activity;
        this.onSessionBookedListener = listener;
    }

    @NonNull
    @Override
    public CourseListView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_list_course_results, parent, false);
        return new CourseListView(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseListView holder, int position) {
        final Session s = this.courseSession.get(position);
        final Tutor tutorInfo = this.tutorMap.get(s.getTutorPhoneNumber());

        // Handle case where tutor is not found (to avoid app crashing)
        if (tutorInfo == null) {
            holder.getRatingBar().setRating(0);
            holder.getRatingText().setText("N/A");
        } else {
            holder.getRatingBar().setRating(tutorInfo.getAvgRatingValue());
            holder.getRatingText().setText(String.valueOf(tutorInfo.getAvgRatingValue()));
        }

        holder.getTutorName().setText(s.getTutorName());
        holder.getDate().setText(s.getDate());
        holder.getTimeRange().setText(MessageFormat.format("{0} to {1}",
                sdf.format(s.getStartTime()), sdf.format(s.getEndTime())));

        RequestSessionButtonListener rsbl = new RequestSessionButtonListener(holder.getRequestSession(), s, this.studentPhoneNumber, this.studentName, this.activity, this.onSessionBookedListener);
        holder.getRequestSession().setOnClickListener(rsbl);
    }

    @Override
    public int getItemCount() {
        return this.courseSession.size();
    }

    public void updateData(List<Session> sessionList) {
        this.courseSession.clear();
        this.courseSession.addAll(sessionList);
        notifyDataSetChanged();
    }

    public void clearData() {
        this.courseSession.clear();
        notifyDataSetChanged();
    }

    public void removeSession(Session session) {
        int index = this.courseSession.indexOf(session);
        if (index >= 0) {
            this.courseSession.remove(index);
            notifyItemRemoved(index);
        }
    }

    List<Session> getCourseSession() {
        return this.courseSession;
    }
}