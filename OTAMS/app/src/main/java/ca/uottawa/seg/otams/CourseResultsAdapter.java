package ca.uottawa.seg.otams;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CourseResultsAdapter extends RecyclerView.Adapter<CourseResultsAdapter.ViewHolder> {

    private final List<Session> sessions;
    private final OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(Session session);
    }

    public CourseResultsAdapter(List<Session> sessions, OnBookClickListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_course_results, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Session session = sessions.get(position);

        holder.tutorName.setText(session.getTutorName());
        holder.course.setText(session.getTutorName() + " offers " + session.getTutorName()); // we’ll update
        holder.rating.setText("Rating: " + session.getTutorPhoneNumber()); // we’ll update
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.date.setText(session.getDate());
        holder.time.setText(sdf.format(session.getStartTime()) + " - " + sdf.format(session.getEndTime()));

        holder.bookButton.setOnClickListener(v -> listener.onBookClick(session));
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tutorName, date, time, course, rating;
        Button bookButton;

        ViewHolder(View itemView) {
            super(itemView);
            tutorName = itemView.findViewById(R.id.list_course_results_tutorName);
            date = itemView.findViewById(R.id.list_course_results_date);
            time = itemView.findViewById(R.id.list_course_results_time);
            course = itemView.findViewById(R.id.list_course_results_courses);
            rating = itemView.findViewById(R.id.list_course_results_rating);
            bookButton = itemView.findViewById(R.id.button_course_results_book);
        }
    }
}
