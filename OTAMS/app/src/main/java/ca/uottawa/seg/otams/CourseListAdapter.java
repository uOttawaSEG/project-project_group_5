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
import java.util.Objects;

public class CourseListAdapter extends RecyclerView.Adapter<CourseListView> {
    private static class RequestSessionButtonListener implements View.OnClickListener {

        private final Button buttonRef;
        private final Session session;

        private final String studentPhoneNumber;

        private RequestSessionButtonListener(Button btn, Session s, String phoneNumber) {
            this.buttonRef = btn;
            this.session = s;
            this.studentPhoneNumber = phoneNumber;
        }

        @Override
        public void onClick(View v) {
            this.buttonRef.setActivated(false);

            /**
             * Connect to Firebase and set session to PENDING or to APPROVED if auto-approve
             */

            DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
            String bookingId = bookingsRef.push().getKey();

            Map<String, Object> bookingData = new HashMap<>();
            bookingData.put("sessionId", this.session.getId());
            bookingData.put("tutorName", this.session.getTutorName());
            bookingData.put("tutorPhoneNumber", this.session.getTutorPhoneNumber());
            bookingData.put("studentPhoneNumber", this.studentPhoneNumber);
            bookingData.put("status", "PENDING");
            bookingData.put("timestamp", System.currentTimeMillis());
            bookingData.put("courses", this.session.getCourses());
            bookingData.put("date", this.session.getDate());
            bookingData.put("startTime", this.session.getStartTime());
            bookingData.put("endTime", this.session.getEndTime());

            bookingsRef.child(bookingId).setValue(bookingData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this.buttonRef.getContext(), "Session requested!", Toast.LENGTH_SHORT).show();
                    // Optionally update session status to PENDING or BOOKED
                    updateSessionStatus(this.session.getId());
                } else {
                    Toast.makeText(this.buttonRef.getContext(), "Failed to request session", Toast.LENGTH_SHORT).show();
                    this.buttonRef.setEnabled(true);
                }
            });
        }

        private void updateSessionStatus(String sessionId) {
            DatabaseReference sessionRef = FirebaseDatabase.getInstance().getReference("sessions").child(sessionId);
            // Update to PENDING after student requests
            sessionRef.child("sessionStatus").setValue(SessionStatus.PENDING.toString());
        }
    }
    private final List<Session> courseSession;
    private final Map<String, Tutor> tutorMap;
    private final String studentPhoneNumber;

    public CourseListAdapter(Map<String, Tutor> tutorMap, String studentPhoneNumber) {
        this.courseSession = new ArrayList<>();
        this.tutorMap = tutorMap;
        this.studentPhoneNumber = studentPhoneNumber;
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
        final Tutor tutorInfo = Objects.requireNonNull(this.tutorMap.get(s.getTutorName()));

        holder.getTutorName().setText(s.getTutorName());
        holder.getDate().setText(s.getDate());
        holder.getTimeRange().setText(MessageFormat.format("{0} to {1}",
                sdf.format(s.getStartTime()), sdf.format(s.getEndTime())));
        holder.getRatingBar().setRating(tutorInfo.getAvgRatingValue());
        holder.getRatingText().setText(String.valueOf(tutorInfo.getAvgRatingValue()));

        RequestSessionButtonListener rsbl = new RequestSessionButtonListener(
                holder.getRequestSession(), s, this.studentPhoneNumber);
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


    List<Session> getCourseSession() {
        return this.courseSession;
    }

}
