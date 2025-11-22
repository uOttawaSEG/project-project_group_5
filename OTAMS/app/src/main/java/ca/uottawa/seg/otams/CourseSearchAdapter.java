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

public class CourseSearchAdapter extends RecyclerView.Adapter<CourseSearchAdapter.ViewHolder> {

    private final List<Session> sessions;
    private final OnBookClickListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.CANADA);

    public interface OnBookClickListener {
        void onBook(Session s);
    }

    public CourseSearchAdapter(List<Session> sessions, OnBookClickListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_list_course_results, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Session s = sessions.get(position);

        holder.tutorName.setText(s.getTutorName());
        holder.date.setText(s.getDate());
        holder.time.setText(sdf.format(s.getStartTime()) + " - " + sdf.format(s.getEndTime()));
        holder.rating.setText("Rating: " + "N/A"); // update when rating implemented
        holder.courses.setText("Course: ???"); // your Session object currently does NOT store course code

        holder.bookButton.setOnClickListener(v -> listener.onBook(s));
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tutorName, date, time, rating, courses;
        Button bookButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tutorName = itemView.findViewById(R.id.list_course_results_tutorName);
            date = itemView.findViewById(R.id.list_course_results_date);
            time = itemView.findViewById(R.id.list_course_results_time);
            courses = itemView.findViewById(R.id.list_course_results_courses);
            rating = itemView.findViewById(R.id.list_course_results_rating);
            bookButton = itemView.findViewById(R.id.button_course_results_book);
        }
    }
}
