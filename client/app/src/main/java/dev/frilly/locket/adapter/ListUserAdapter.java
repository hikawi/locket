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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.frilly.locket.Constants;
import dev.frilly.locket.R;
import dev.frilly.locket.activities.ChatActivity;
import dev.frilly.locket.model.User;
import dev.frilly.locket.room.LocalDatabase;
import dev.frilly.locket.room.entities.UserProfile;
import com.google.firebase.firestore.FirebaseFirestoreException;

import dev.frilly.locket.R;
import dev.frilly.locket.activities.ChatActivity;
import dev.frilly.locket.model.User;
import dev.frilly.locket.utils.AndroidUtil;
import dev.frilly.locket.utils.FirebaseUtil;

public class ListUserAdapter extends FirestoreRecyclerAdapter<User, ListUserAdapter.UserViewHolder> {

    private static final String TAG = "ListUserAdapter";
    private static Map<String, String> friendAvatarUrls = new HashMap<>(); // userId -> avatarUrl

    public ListUserAdapter(@NonNull FirestoreRecyclerOptions<User> options) {
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
        String avatarUrl = friendAvatarUrls.get(model.getUserId());
        if (avatarUrl != null) {
            holder.imageProfile.setColorFilter(null);

            Glide.with(holder.itemView.getContext())
                    .load(avatarUrl)
                    .circleCrop()
                    .into(holder.imageProfile);

            // Nếu có avatar => clear tint
        } else {
            // Nếu không có avatar => set hình mặc định + tint
            holder.imageProfile.setImageResource(R.drawable.person_icon);
            holder.imageProfile.setColorFilter(holder.itemView.getContext().getColor(R.color.slate));
        }

        // Xử lý sự kiện click vào user
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ChatActivity.class);
            AndroidUtil.passUserModelAsIntent(intent, model);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            v.getContext().startActivity(intent);
        });
    }

    public static void fetchFriendsAndLoadUsers(FirestoreRecyclerAdapter<User, ListUserAdapter.UserViewHolder> adapter) {
        // Lấy LocalDatabase từ Constants.ROOM
        LocalDatabase db = Constants.ROOM;

        LiveData<List<UserProfile>> friendsLiveData = db.userProfileDao().getProfiles();
        friendsLiveData.observeForever(new Observer<List<UserProfile>>() {
            @Override
            public void onChanged(List<UserProfile> profiles) {
                if (profiles == null || profiles.isEmpty()) {
                    Log.d(TAG, "No friends found.");
                    return;
                }

                List<String> friendIds = profiles.stream()
                        .filter(profile -> profile.friendState == UserProfile.FriendState.FRIEND)
                        .map(profile -> String.valueOf(profile.id)) // Chuyển id thành String
                        .collect(Collectors.toList());

                friendAvatarUrls.clear();
                for (UserProfile profile : profiles) {
                    if (profile.friendState == UserProfile.FriendState.FRIEND) {
                        friendAvatarUrls.put(String.valueOf(profile.id), profile.avatarUrl);
                    }
                }
                Log.d(TAG, "Friend IDs: " + friendIds);
                Log.d(TAG, "Raw friend profiles: " + profiles);
                Log.d(TAG, "Friend IDs extracted: " + friendIds);
                if (friendIds.isEmpty()) {
                    Log.d(TAG, "No valid friend IDs.");
                    return;
                }

                Query query = FirebaseFirestore.getInstance().collection("users")
                        .whereIn("userId", friendIds);

                FirestoreRecyclerOptions<User> newOptions = new FirestoreRecyclerOptions.Builder<User>()
                        .setQuery(query, User.class)
                        .build();

                adapter.updateOptions(newOptions);
            }
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
