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
            Intent intent = new Intent(v.getContext(), SessionDetailsActivity.class); // Set the next page

            // Pass the details of the user who made the request (excluding their password) to the next page so it can be displayed there
            intent.putExtra(SessionDetailsActivity.STUDENT_NAME, s.getStudentName());
            intent.putExtra(SessionDetailsActivity.PHONE_NUMBER, s.getStudentPhoneNumber());
            // Send the administrator to the detailed request page associated with the arrow the administrator clicked
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
