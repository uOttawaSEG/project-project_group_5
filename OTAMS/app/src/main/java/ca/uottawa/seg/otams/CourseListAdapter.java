package ca.uottawa.seg.otams;

import static ca.uottawa.seg.otams.ManageAvailabilityActivity.sdf;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CourseListAdapter extends RecyclerView.Adapter<CourseListView> {

    private static class RequestSessionButtonListener implements View.OnClickListener {

        private final Button buttonRef;
        private final Session session;

        private RequestSessionButtonListener(Button btn, Session s) {
            this.buttonRef = btn;
            this.session = s;
        }

        @Override
        public void onClick(View v) {
            this.buttonRef.setActivated(false);
            /**
             * Connect to Firebase and set session to PENDING
             */
        }
    }
    private final List<Session> courseSession;
    private final Map<String, Tutor> tutorMap;

    public CourseListAdapter(List<Session> courseSession, Map<String, Tutor> tutorMap) {
        this.courseSession = courseSession;
        this.tutorMap = tutorMap;
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
        holder.getTimeRange().setText(MessageFormat.format("{0} to {1}", sdf.format(s.getStartTime()), sdf.format(s.getEndTime())));
        holder.getRatingBar().setRating(tutorInfo.getAvgRatingValue());
        holder.getRatingText().setText(String.valueOf(tutorInfo.getAvgRatingValue()));
        RequestSessionButtonListener rsbl = new RequestSessionButtonListener(holder.getRequestSession(), s);
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

}
