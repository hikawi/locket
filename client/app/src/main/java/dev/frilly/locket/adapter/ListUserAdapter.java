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

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.stream.Collectors;

import dev.frilly.locket.Constants;
import dev.frilly.locket.R;
import dev.frilly.locket.activities.ChatActivity;
import dev.frilly.locket.model.User;
import dev.frilly.locket.room.LocalDatabase;
import dev.frilly.locket.room.entities.UserProfile;
import dev.frilly.locket.utils.AndroidUtil;
import dev.frilly.locket.utils.FirebaseUtil;

public class ListUserAdapter extends FirestoreRecyclerAdapter<User, ListUserAdapter.UserViewHolder> {

    private static final String TAG = "ListUserAdapter";

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

        Log.d(TAG, "User: " + model.getUsername() + " - ID: " + model.getUserId());

        FirebaseUtil.getOtherProfilePicStorageRef(model.getUserId()).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri uri = task.getResult();
                        AndroidUtil.setProfilePic(holder.itemView.getContext(), uri, holder.imageProfile);
                    }
                });

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
