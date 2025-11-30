package ca.uottawa.seg.otams;

import static ca.uottawa.seg.otams.ManageAvailabilityActivity.sdf;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.MessageFormat;
import java.util.List;

public class SessionListAdapter extends RecyclerView.Adapter<SessionListView> {

    private final List<Session> sessionList;
    private boolean isStudentView = false; // Flag to determine if this is being used in student dashboard

    public SessionListAdapter(List<Session> sessionList) {
        this.sessionList = sessionList;
    }

    public SessionListAdapter(List<Session> sessionList, boolean isStudentView) {
        this.sessionList = sessionList;
        this.isStudentView = isStudentView;
    }

    @NonNull
    @Override
    public SessionListView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_list_student_tutor_request, parent, false);
        return new SessionListView(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionListView holder, int position) {
        final Session s = this.sessionList.get(position);
        holder.getDateAndTime().setText(MessageFormat.format("{0} {1}-{2}", s.getDate(), sdf.format(s.getStartTime()), sdf.format(s.getEndTime())));

        // Display appropriate name based on whose dashboard this is
        if (isStudentView) {
            holder.getStudentName().setText(s.getTutorName());
        } else {
            holder.getStudentName().setText(s.getStudentName());
        }

        holder.getArrow().setOnClickListener(v -> {
            Intent intent;
            String sessionStatus = s.getSessionStatus();

            // Different navigation for tutor vs student
            if (isStudentView) {
                // Student dashboard navigation
                if (sessionStatus.equals("PENDING")) {
                    intent = new Intent(v.getContext(), StudentInfoPendingActivity.class);
                } else if (sessionStatus.equals("APPROVED")) {
                    intent = new Intent(v.getContext(), StudentInfoUpcomingActivity.class);
                } else if (sessionStatus.equals("COMPLETED")) {
                    intent = new Intent(v.getContext(), StudentInfoPastActivity.class);
                    // Pass student phone number for rating functionality
                    intent.putExtra("studentPhoneNumber", s.getStudentPhoneNumber());
                } else if (sessionStatus.equals("REJECTED")) {
                    intent = new Intent(v.getContext(), StudentInfoRejectedActivity.class);
                } else {
                    return; // Unknown status
                }
            } else {
                // Tutor dashboard navigation (original logic)
                if (sessionStatus.equals("OPEN")) {
                    intent = new Intent(v.getContext(), OpenTimeslotPageActivity.class);
                } else if (sessionStatus.equals("PENDING")) {
                    intent = new Intent(v.getContext(), StudentInformationActivity.class);
                    intent.putExtra(StudentInformationActivity.STUDENT_NAME, s.getStudentName());
                    intent.putExtra(StudentInformationActivity.PHONE_NUMBER, s.getStudentPhoneNumber());
                } else if (sessionStatus.equals("APPROVED")) {
                    intent = new Intent(v.getContext(), StudentInformationActivity.class);
                    intent.putExtra(StudentInformationActivity.STUDENT_NAME, s.getStudentName());
                    intent.putExtra(StudentInformationActivity.PHONE_NUMBER, s.getStudentPhoneNumber());
                } else if (sessionStatus.equals("COMPLETED")) {
                    intent = new Intent(v.getContext(), StudentInformationActivity.class);
                    intent.putExtra(StudentInformationActivity.STUDENT_NAME, s.getStudentName());
                    intent.putExtra(StudentInformationActivity.PHONE_NUMBER, s.getStudentPhoneNumber());
                } else {
                    intent = new Intent(v.getContext(), StudentInformationActivity.class);
                }
            }

            intent.putExtra("id", s.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return this.sessionList.size();
    }

    public void updateData(List<Session> sessionList) {
        this.sessionList.clear();
        this.sessionList.addAll(sessionList);
        notifyDataSetChanged();
    }

    public void clearData() {
        this.sessionList.clear();
        notifyDataSetChanged();
    }

    public List<Session> getSessionList() {
        return sessionList;
    }
}