package ca.uottawa.seg.otams;

import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

public class CourseListView extends RecyclerView.ViewHolder {

    private final TextView tutorName;
    private final TextView date;

    private final TextView timeRange;

    private final RatingBar ratingBar;
    private final TextView ratingText;

    private final Button requestSession;

    public CourseListView(@NonNull View itemView) {
        super(itemView);
        this.tutorName = itemView.findViewById(R.id.tvTutorName);
        this.date = itemView.findViewById(R.id.tvDate);
        this.timeRange = itemView.findViewById(R.id.tvTimeRange);
        this.ratingBar = itemView.findViewById(R.id.tvRatingBar);
        this.ratingText = itemView.findViewById(R.id.tvRatingText);
        this.requestSession = itemView.findViewById(R.id.tvBtnRequestSession);
    }

    public TextView getTutorName() {
        return tutorName;
    }

    public TextView getDate() {
        return date;
    }

    public TextView getTimeRange() {
        return timeRange;
    }

    public RatingBar getRatingBar() {
        return ratingBar;
    }

    public TextView getRatingText() {
        return ratingText;
    }

    public Button getRequestSession() {
        return requestSession;
    }
}
