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
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import dev.frilly.locket.R;
import dev.frilly.locket.activities.ChatActivity;
import dev.frilly.locket.model.Chatroom;
import dev.frilly.locket.model.User;
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
                Log.e("ChatroomAdapter", "Không tìm thấy user còn lại!");
                return;
            }

            otherUserRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUtil.getCurrentUserId(currentUserId -> {
                        if (currentUserId == null) {
                            Log.e("ChatroomAdapter", "User ID is null, cannot determine message sender.");
                            return;
                        }

                        boolean lastMessageSentByMe = model.getLastMessageSenderId() != null &&
                                model.getLastMessageSenderId().equals(currentUserId);

                        User otherUserModel = task.getResult().toObject(User.class);
                        if (otherUserModel == null) {
                            Log.e("ChatroomAdapter", "otherUserModel is null");
                            return;
                        }

                        FirebaseUtil.getOtherProfilePicStorageRef(otherUserModel.getUserId()).getDownloadUrl()
                                .addOnCompleteListener(t -> {
                                    if (t.isSuccessful() && t.getResult() != null) {
                                        Uri uri = t.getResult();
                                        AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                                    } else {
                                        Log.e("ChatroomAdapter", "Failed to load profile pic");
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
                }
            });
        });
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
