package ca.uottawa.seg.otams;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SessionListView extends RecyclerView.ViewHolder {

    private final TextView dateAndTime;
    private final TextView studentName;
    private final TextView arrow; // Tracks the arrow image corresponding to the request

    public SessionListView(@NonNull View itemView) {
        super(itemView);
        this.dateAndTime = itemView.findViewById(R.id.request_name_text_or_date);
        this.studentName = itemView.findViewById(R.id.request_role_text_or_name);
        this.arrow = itemView.findViewById(R.id.arrow_text);
    }

    public TextView getDateAndTime() {
        return dateAndTime;
    }

    public TextView getStudentName() {
        return studentName;
    }

    public TextView getArrow() {
        return arrow;
    }
}