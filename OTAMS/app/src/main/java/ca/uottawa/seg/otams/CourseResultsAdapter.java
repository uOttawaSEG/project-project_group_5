package ca.uottawa.seg.otams;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CourseResultsAdapter extends RecyclerView.Adapter<CourseResultsAdapter.VH> {

    private final List<CourseSearchResultsActivity.CourseResult> list;

    public CourseResultsAdapter(List<CourseSearchResultsActivity.CourseResult> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_course_result, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CourseSearchResultsActivity.CourseResult r = list.get(position);
        holder.tutorName.setText(r.tutorName);
        holder.date.setText(r.date == null ? "" : r.date);
        holder.time.setText(r.time == null ? "" : r.time);
        holder.courses.setText(r.courses == null ? "" : r.courses);
        holder.rating.setText(r.rating == null ? "No rating" : "Rating: " + r.rating);

        holder.bookBtn.setOnClickListener(v -> {
            // TODO: implement booking flow (request session)
            // For now, start a placeholder Activity or show a toast.
            Intent intent = new Intent(v.getContext(), RequestSessionActivity.class);
            intent.putExtra("sessionId", r.sessionId);
            intent.putExtra("tutorPhone", r.tutorPhone);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tutorName, date, time, courses, rating;
        Button bookBtn;

        VH(@NonNull View itemView) {
            super(itemView);
            tutorName = itemView.findViewById(R.id.list_course_results_tutorName);
            date = itemView.findViewById(R.id.list_course_results_date);
            time = itemView.findViewById(R.id.list_course_results_time);
            courses = itemView.findViewById(R.id.list_course_results_courses);
            rating = itemView.findViewById(R.id.list_course_results_rating);
            bookBtn = itemView.findViewById(R.id.button_course_results_book);
        }
    }
}
