package dev.frilly.locket.adapter;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestoreException;
import dev.frilly.locket.R;
import dev.frilly.locket.model.User;
import dev.frilly.locket.utils.AndroidUtil;
import dev.frilly.locket.utils.FirebaseUtil;
import dev.frilly.locket.activities.ChatActivity;

public class ListUserApdater extends FirestoreRecyclerAdapter<User, ListUserApdater.UserViewHolder> {

    String currentUserId;


    public ListUserApdater(@NonNull FirestoreRecyclerOptions<User> options) {
        super(options);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mess_user_recycle_row, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
        holder.textUserName.setText(model.getUsername());
        holder.textPhoneNumber.setText(model.getEmail());

        // Ghi log để kiểm tra ID
        Log.d("ListUserAdapter", "User: " + model.getUsername() + " - ID: " + model.getUserId());

        // Lấy userId thực sự từ Firestore và kiểm tra
        FirebaseUtil.getCurrentUserId(userId -> {
            Log.d("ListUserAdapter", "Current User ID: " + userId);
            if (userId != null && userId.equals(model.getUserId())) {
                holder.textUserName.setText(model.getUsername() + " (Me)");
            }
        });

        // Load ảnh đại diện từ Firebase Storage
        FirebaseUtil.getOtherProfilePicStorageRef(model.getUserId()).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri uri = task.getResult();
                        AndroidUtil.setProfilePic(holder.itemView.getContext(), uri, holder.imageProfile);
                    }
                });

        // Xử lý sự kiện click vào user
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ChatActivity.class);
            AndroidUtil.passUserModelAsIntent(intent, model);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            v.getContext().startActivity(intent);
        });
    }


    @Override
    public void onError(@NonNull FirebaseFirestoreException e) {
        super.onError(e);
        e.printStackTrace();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textUserName, textPhoneNumber;
        ImageView imageProfile;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textUserName = itemView.findViewById(R.id.text_user_name);
            textPhoneNumber = itemView.findViewById(R.id.text_phone_number);
            imageProfile = itemView.findViewById(R.id.profile_pic);
        }
    }
}
