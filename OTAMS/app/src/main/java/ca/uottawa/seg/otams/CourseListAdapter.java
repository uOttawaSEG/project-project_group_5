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

            // Check for time conflicts only if activity is available
            if (activity != null && activity.hasTimeConflict(session.getStartTime(), session.getEndTime())) {
                Toast.makeText(this.buttonRef.getContext(),
                        "You have a scheduling conflict with another session",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            this.buttonRef.setEnabled(false);

            DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("sessions");
            String sessionId = this.session.getId();

            // Determine if session should be auto-approved
            String sessionStatus = this.session.getAutoApprove() ?
                    SessionStatus.APPROVED.toString() : SessionStatus.PENDING.toString();

            Map<String, Object> bookingData = new HashMap<>();
            bookingData.put("sessionStatus", sessionStatus);
            bookingData.put("studentPhoneNumber", this.studentPhoneNumber);
            bookingData.put("studentName", this.studentName);

            bookingsRef.child(sessionId).updateChildren(bookingData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this.buttonRef.getContext(), "Session requested!", Toast.LENGTH_SHORT).show();

                    // Update the session object and notify the student
                    this.session.setSessionStatus(this.session.getAutoApprove() ?
                            SessionStatus.APPROVED : SessionStatus.PENDING);
                    this.session.setStudentPhoneNumber(this.studentPhoneNumber);
                    this.session.setStudentName(this.studentName);

                    // Notify callback to remove from list
                    if (callback != null) {
                        callback.onSessionBooked(this.session);
                    }
                } else {
                    Toast.makeText(this.buttonRef.getContext(), "Failed to request session: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

        // Handle case where tutor is not found
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