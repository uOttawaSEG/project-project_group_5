package ca.uottawa.seg.otams;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListView> {

    private final List<User> userList;

    public UserListAdapter(List<User> userList) {
        this.userList = userList;
    }

    @SuppressLint("ResourceType")
    @NonNull
    @Override
    public UserListView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_list_student_tutor_request, parent, false);
        return new UserListView(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserListView holder, int position) {
        User u = this.userList.get(position);
        String firstLastName = u.getFirstName() + " " + u.getLastName();
        holder.getName().setText(firstLastName);
        holder.getRole().setText(u.getRole());
    }

    @Override
    public int getItemCount() {
        return this.userList.size();
    }

    public void updateData(List<User> newList) {
        this.userList.clear();
        this.userList.addAll(newList);
        notifyDataSetChanged();
    }

    public void clearData() {
        this.userList.clear();
        notifyDataSetChanged();
    }
}
