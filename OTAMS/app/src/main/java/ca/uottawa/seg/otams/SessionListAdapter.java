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

    public SessionListAdapter(List<Session> sessionList) {
        this.sessionList = sessionList;
    }

    @NonNull
    @Override
    public SessionListView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_list_student_tutor_request, parent, false);
        return new SessionListView(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionListView holder, int position) {
        Session s = this.sessionList.get(position);
        holder.getDateAndTime().setText(MessageFormat.format("{0} {1}-{2}", s.getDate(), sdf.format(s.getStartTime()), sdf.format(s.getEndTime())));
        holder.getStudentName().setText(s.getStudentName());
        holder.getArrow().setOnClickListener(v -> {
            // Send the tutor to a different page when they click the arrow depending on the status of the session
            Intent intent;
            if (s.getSessionStatus().equals("OPEN")) {
                intent = new Intent(v.getContext(), OpenTimeslotPageActivity.class); // Set the next page
            }
            else if (s.getSessionStatus().equals("PENDING")) {
                intent = new Intent(v.getContext(), StudentInformationActivity.class); // Set the next page

                // Pass the details of the user who made the request (excluding their password) to the next page so it can be displayed there
                intent.putExtra(StudentInformationActivity.STUDENT_NAME, s.getStudentName());
                intent.putExtra(StudentInformationActivity.PHONE_NUMBER, s.getStudentPhoneNumber());
            }
            else if (s.getSessionStatus().equals("APPROVED")) {
                intent = new Intent(v.getContext(), StudentInfoUpcomingActivity.class); // Set the next page

                // Pass the details of the user who made the request (excluding their password) to the next page so it can be displayed there
                intent.putExtra(StudentInformationActivity.STUDENT_NAME, s.getStudentName());
                intent.putExtra(StudentInformationActivity.PHONE_NUMBER, s.getStudentPhoneNumber());
            }
            else if (s.getSessionStatus().equals("COMPLETED")) {
                intent = new Intent(v.getContext(), StudentInfoPastActivity.class); // Set the next page

                // Pass the details of the user who made the request (excluding their password) to the next page so it can be displayed there
                intent.putExtra(StudentInformationActivity.STUDENT_NAME, s.getStudentName());
                intent.putExtra(StudentInformationActivity.PHONE_NUMBER, s.getStudentPhoneNumber());
            }
            else {
                intent = new Intent(v.getContext(), StudentInformationActivity.class); // Set the next page
            }

            intent.putExtra("id", s.getId()); // Pass the session ID (key in the database) to allow easy identification of which request the tutor is trying to approve/reject
            // Send the user to the detailed request page associated with the arrow the user clicked
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
