package ca.uottawa.seg.otams;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserListView extends RecyclerView.ViewHolder {

    private final TextView name;
    private final TextView role;

    public UserListView(@NonNull View itemView) {
        super(itemView);
        this.name = itemView.findViewById(R.id.request_name_text);
        this.role = itemView.findViewById(R.id.request_role_text);
    }

    public TextView getName() {
        return name;
    }

    public TextView getRole() {
        return role;
    }
}
