package ca.uottawa.seg.otams;

import android.annotation.SuppressLint;
import android.content.Intent;
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

        // Check if the administrator has clicked on an arrow corresponding to a request and would like to see more details about said request
        holder.getArrow().setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AdminRequestDetailsActivity.class); // Set the next page

            // Pass the details of the user who made the request (excluding their password) to the next page so it can be displayed there
            intent.putExtra("role", u.getRole());
            intent.putExtra("firstName", u.getFirstName());
            intent.putExtra("lastName", u.getLastName());
            intent.putExtra("email", u.getEmail());
            intent.putExtra("phoneNumber", u.getPhoneNumber());

            // Collect extra details depending on whether the request is from a user who wishes to be a student or tutor
            if (u instanceof Student) {
                Student s = (Student) u;

                intent.putExtra("program", s.getProgram());
            }
            else if (u instanceof Tutor) {
                Tutor t = (Tutor) u;

                intent.putExtra("highestDegree", t.getHighestDegree());
                intent.putExtra("coursesOffered", t.getCoursesOffered());
            }

            // Send the administrator to the detailed request page associated with the arrow the administrator clicked
            v.getContext().startActivity(intent);
        });
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
