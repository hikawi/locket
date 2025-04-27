package dev.frilly.locket.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.frilly.locket.Constants;
import dev.frilly.locket.adapter.ChatRecyclerAdapter;
import dev.frilly.locket.Authentication;
import dev.frilly.locket.R;
import dev.frilly.locket.model.ChatMessageModel;
import dev.frilly.locket.model.Chatroom;
import dev.frilly.locket.model.User;
import dev.frilly.locket.room.LocalDatabase;
import dev.frilly.locket.room.entities.UserProfile;
import dev.frilly.locket.utils.AndroidUtil;
import dev.frilly.locket.utils.FirebaseUtil;

public class ChatActivity extends BaseActivity {

    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton backBtn;
    RecyclerView recyclerView;
    TextView otherUserName;
    ImageView avatarImageView;

    ChatRecyclerAdapter adapter;
    User otherUser;
    String chatroomId;
    Chatroom chatRoomModel;

    String currentUserId;
    private static Map<String, String> friendAvatarUrls = new HashMap<>(); // userId -> avatarUrl

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
        FirebaseUtil.getCurrentUserId(userId -> {
            currentUserId = userId;
            Log.d("ChatActivity", "Current User ID: " + currentUserId);
            chatroomId = FirebaseUtil.getChatroomId(currentUserId, otherUser.getUserId());

            getOrCreareChatroomModel();
            setupChatRecyclerView();
            loadAvatar();
            fetchFriendsAndLoadAvatars();  // Fetch avatars of friends
        });

        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        otherUserName = findViewById(R.id.other_user_name);
        recyclerView = findViewById(R.id.chat_recycler_view);
        avatarImageView = findViewById(R.id.profile_pic);

        backBtn.setOnClickListener(v -> finish());

        otherUserName.setText(otherUser.getUsername());

        Log.d("ChatActivity", "Other User ID: " + otherUser.getUserId());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sendMessageBtn.setOnClickListener((v -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty())
                return;
            sendMessageToUser(message);
        }));
    }

    // Add this method to fetch friends and load their avatars
    void fetchFriendsAndLoadAvatars() {
        LocalDatabase db = Constants.ROOM;

        LiveData<List<UserProfile>> friendsLiveData = db.userProfileDao().getProfiles();
        friendsLiveData.observeForever(new Observer<List<UserProfile>>() {
            @Override
            public void onChanged(List<UserProfile> profiles) {
                if (profiles == null || profiles.isEmpty()) {
                    Log.d("ChatActivity", "No friends found.");
                    return;
                }

                // Store friend avatar URLs in the map
                friendAvatarUrls.clear();
                for (UserProfile profile : profiles) {
                    if (profile.friendState == UserProfile.FriendState.FRIEND) {
                        friendAvatarUrls.put(String.valueOf(profile.id), profile.avatarUrl);
                    }
                }
                Log.d("ChatActivity", "Friend Avatar URLs: " + friendAvatarUrls);
                loadAvatar();  // Load the avatar of the other user after fetching friends' data
            }
        });
    }

    void setupChatRecyclerView() {
        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class).build();

        adapter = new ChatRecyclerAdapter(options, getApplicationContext());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    void loadAvatar() {
        String avatarUrl = friendAvatarUrls.get(otherUser.getUserId()); // Fetch the avatar URL based on the user ID
        Log.d("ChatActivity", "Avatar URL: " + avatarUrl);

        if (avatarUrl != null) {
            avatarImageView.setColorFilter(null); // Remove any tint if avatar URL exists
            Glide.with(this)
                    .load(avatarUrl)
                    .circleCrop()
                    .into(avatarImageView); // Ensure this is the correct ImageView in the layout
        } else {
            // Use a default avatar if the URL is not available
            avatarImageView.setImageResource(R.drawable.person_icon); // Fallback to default image
            avatarImageView.setColorFilter(getResources().getColor(R.color.slate)); // Apply tint if necessary
        }
    }

    void sendMessageToUser(String message) {
        Log.d("ChatActivity", "Sending message: " + message);
        FirebaseUtil.getCurrentUserId(userId -> {
            currentUserId = userId;
            chatRoomModel.setLastMessageTimestamp(Timestamp.now());
            chatRoomModel.setLastMessageSenderId(currentUserId);
            chatRoomModel.setLastMessage(message);
            FirebaseUtil.getChatroomReference(chatroomId).set(chatRoomModel);

            ChatMessageModel chatMessageModel = new ChatMessageModel(message, currentUserId, Timestamp.now());
            FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                messageInput.setText("");
                                // sendNotification(message);
                            }
                        }
                    });
        });
    }

    void getOrCreareChatroomModel() {
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener((task) -> {
            if (task.isSuccessful()) {
                chatRoomModel = task.getResult().toObject(Chatroom.class);
                if (chatRoomModel == null) {
                    // first time chat
                    chatRoomModel = new Chatroom(chatroomId, Arrays.asList(currentUserId, otherUser.getUserId()), Timestamp.now(), "");
                    FirebaseUtil.getChatroomReference(chatroomId).set(chatRoomModel);
                    Log.d("ChatActivity", "Creating new chatroom for users: " + currentUserId + " and " + otherUser.getUserId());
                } else {
                    Log.d("ChatActivity", "Chatroom exists. Users: " + chatRoomModel.getUserIds());
                }
            }
        });
    }
}
