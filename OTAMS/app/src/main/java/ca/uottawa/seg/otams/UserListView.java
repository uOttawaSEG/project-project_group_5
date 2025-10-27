package ca.uottawa.seg.otams;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserListView extends RecyclerView.ViewHolder {

    private final TextView name;
    private final TextView role;
    private final TextView arrow; // Tracks the arrow image corresponding to the request

    public UserListView(@NonNull View itemView) {
        super(itemView);
        this.name = itemView.findViewById(R.id.request_name_text);
        this.role = itemView.findViewById(R.id.request_role_text);
        this.arrow = itemView.findViewById(R.id.arrow_text);
    }

    public TextView getName() {
        return name;
    }

    public TextView getRole() {
        return role;
    }

    public TextView getArrow() {
        return arrow;
    } // Returns the arrow belonging to the specific request
}
