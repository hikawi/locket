package dev.frilly.locket.adapter;

import android.content.Context;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import dev.frilly.locket.Constants;
import dev.frilly.locket.R;
import dev.frilly.locket.activities.ChatActivity;
import dev.frilly.locket.model.Chatroom;
import dev.frilly.locket.model.User;
import dev.frilly.locket.room.entities.UserProfile;
import dev.frilly.locket.utils.AndroidUtil;
import dev.frilly.locket.utils.FirebaseUtil;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<Chatroom, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {

    private final Context context;

    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<Chatroom> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int position, @NonNull Chatroom model) {
        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds(), otherUserRef -> {
            if (otherUserRef == null) {
                Log.e("RecentChatsActivity", "Không tìm thấy user còn lại!");
                return;
            }

            otherUserRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUtil.getCurrentUserId(currentUserId -> {
                        if (currentUserId == null) {
                            Log.e("RecentChatsActivity", "User ID is null, cannot determine message sender.");
                            return;
                        }

                        boolean lastMessageSentByMe = model.getLastMessageSenderId() != null &&
                                model.getLastMessageSenderId().equals(currentUserId);

                        User otherUserModel = task.getResult().toObject(User.class);
                        if (otherUserModel == null) {
                            Log.e("RecentChatsActivity", "otherUserModel is null");
                            return;
                        }

                        // 🔥 Kiểm tra xem user này có phải bạn bè không bằng LocalDatabase
                        Constants.ROOM.userProfileDao().getProfiles().observeForever(profiles -> {
                            if (profiles == null || profiles.isEmpty()) {
                                Log.d("RecentChatsActivity", "Không có danh sách bạn bè.");
                                return;
                            }

                            boolean isFriend = profiles.stream()
                                    .anyMatch(profile -> profile.friendState == UserProfile.FriendState.FRIEND &&
                                            String.valueOf(profile.id).equals(otherUserModel.getUserId()));

                            if (!isFriend) {
                                Log.d("RecentChatsActivity", "User không phải bạn bè, ẩn khỏi danh sách.");
                                getSnapshots().getSnapshot(position).getReference().delete();
                                notifyItemRemoved(position);
                                return;
                            }

                            // 🔥 Nếu là bạn bè, tiếp tục hiển thị
                            FirebaseUtil.getOtherProfilePicStorageRef(otherUserModel.getUserId()).getDownloadUrl()
                                    .addOnCompleteListener(t -> {
                                        if (t.isSuccessful() && t.getResult() != null) {
                                            Uri uri = t.getResult();
                                            AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                                        } else {
                                            Log.e("RecentChatsActivity", "Failed to load profile pic");
                                        }
                                    });

                            holder.usernameText.setText(otherUserModel.getUsername());
                            holder.lastMessageText.setText(lastMessageSentByMe ? "You: " + model.getLastMessage() : model.getLastMessage());
                            holder.lastMessageTime.setText(FirebaseUtil.timestampToString(model.getLastMessageTimestamp()));

                            holder.itemView.setOnClickListener(v -> {
                                Intent intent = new Intent(context, ChatActivity.class);
                                AndroidUtil.passUserModelAsIntent(intent, otherUserModel);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            });
                        });
                    });
                }
            });
        });
    }

    public static void getFriendsList(String userId, OnSuccessListener<List<String>> onSuccess) {
        FirebaseFirestore.getInstance()
                .collection("friends") // Giả sử danh sách bạn bè được lưu trong collection này
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> friendsList = (List<String>) documentSnapshot.get("friendIds");
                        onSuccess.onSuccess(friendsList);
                    } else {
                        onSuccess.onSuccess(new ArrayList<>()); // Nếu không có bạn bè
                    }
                })
                .addOnFailureListener(e -> onSuccess.onSuccess(new ArrayList<>()));
    }

    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false);
        return new ChatroomModelViewHolder(view);
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        notifyDataSetChanged(); // Cập nhật lại danh sách
        Log.d("ChatroomAdapter", "Updated chatroom count: " + getItemCount());
    }

    static class ChatroomModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, lastMessageText, lastMessageTime;
        ImageView profilePic;

        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time_text);
            //profilePic = itemView.findViewById(R.id.profile_pic_image_view);
        }
    }
}
