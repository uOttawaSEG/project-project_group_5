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
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_list_student_tutor_request, parent, false);
        return new SessionListView(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionListView holder, int position) {
        Session s = sessionList.get(position);
        holder.getDateAndTime().setText(
                MessageFormat.format("{0} {1}-{2}", s.getDate(), sdf.format(s.getStartTime()), sdf.format(s.getEndTime()))
        );
        holder.getStudentName().setText(s.getStudentName());

        holder.getArrow().setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), StudentInformationActivity.class);
            intent.putExtra(StudentInformationActivity.STUDENT_NAME, s.getStudentName());
            intent.putExtra(StudentInformationActivity.PHONE_NUMBER, s.getStudentPhoneNumber());
            intent.putExtra("id", s.getId()); // Pass session ID
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    public void updateData(List<Session> sessions) {
        sessionList.clear();
        sessionList.addAll(sessions);
        notifyDataSetChanged();
    }

    public void clearData() {
        sessionList.clear();
        notifyDataSetChanged();
    }

    public List<Session> getSessionList() {
        return sessionList;
    }
}
