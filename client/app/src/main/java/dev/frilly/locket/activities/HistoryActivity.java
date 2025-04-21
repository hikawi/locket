package dev.frilly.locket.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import dev.frilly.locket.Authentication;
import dev.frilly.locket.Constants;
import dev.frilly.locket.R;
import dev.frilly.locket.adapter.MediaAdapter;
import dev.frilly.locket.model.Post;
import dev.frilly.locket.model.PostCache;
import dev.frilly.locket.room.entities.UserProfile;

public class HistoryActivity extends BaseActivity {
    private RecyclerView imageRecyclerView;
    private MediaAdapter mediaAdapter;
    private ArrayList<Post> postList = new ArrayList<>();
    private ArrayList<Post> filteredPostList = new ArrayList<>();
    private Context context;
    private Spinner userFilterSpinner;
    private String selectedUser = "Everyone";
    private ActivityResultLauncher<Intent> friendsPostLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_screen);

        imageRecyclerView = findViewById(R.id.image_recycler_view);
        userFilterSpinner = findViewById(R.id.user_filter_spinner);
        context = getApplicationContext();

        ImageButton userAvatar = findViewById(R.id.user_avatar);
        ImageButton sendMessage = findViewById(R.id.message);
        ImageButton returnButton = findViewById(R.id.return_button);

        friendsPostLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        finish(); // finish HistoryActivity and go back to CameraActivity
                    }
                }
        );

        // Set Grid Layout with 3 columns
        imageRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        mediaAdapter = new MediaAdapter(filteredPostList, context, position -> {
            Post post = filteredPostList.get(position);

            // Debug log to check if correct URL is passed
            if (post.getFileUrl() == null || post.getFileUrl().isEmpty()) {
                Toast.makeText(context, "Error: Image URL is empty!", Toast.LENGTH_SHORT).show();
            }

            Intent friendPostIntent = new Intent(HistoryActivity.this, FriendsPostActivity.class);
            friendPostIntent.putExtra("fileUrl", post.getFileUrl());
            friendPostIntent.putExtra("message", post.getMessage());
            friendPostIntent.putExtra("username", post.getUsername());
            friendPostIntent.putExtra("postTime", post.getPostTime());
            friendsPostLauncher.launch(friendPostIntent);
        });


        imageRecyclerView.setAdapter(mediaAdapter);

        // Set up the list of users
        try {
            setupSpinner();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Show all the posts
        loadFromCache();

        userAvatar.setOnClickListener(v -> {
            final var profileIntent = new Intent(HistoryActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        });

        sendMessage.setOnClickListener(v -> {
            final var chatIntent = new Intent(HistoryActivity.this, RecentChatsActivity.class);
            startActivity(chatIntent);
        });

        returnButton.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
    }

    private void setupSpinner() throws JSONException {
        List<String> users = new ArrayList<>();
        users.add("Everyone");

        String currentUser = Authentication.getUserData(this).getString("username");
        users.add(currentUser);

        // Add friends' usernames from local database
        Constants.ROOM.userProfileDao().getProfiles().observe(this, userProfiles -> {
            for (UserProfile profile : userProfiles) {
                if (profile.friendState == UserProfile.FriendState.FRIEND && !users.contains(profile.username)) {
                    users.add(profile.username);
                }
            }

            // Add usernames from PostCache (in case someone not in friends made a post)
            for (String username : PostCache.getInstance().getUsernames()) {
                if (!users.contains(username)) {
                    users.add(username);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, users);
            adapter.setDropDownViewResource(R.layout.spinner_drop_item);
            userFilterSpinner.setAdapter(adapter);
            userFilterSpinner.post(() -> {
                    userFilterSpinner.setDropDownWidth(userFilterSpinner.getWidth());
                    userFilterSpinner.setSelection(0, false);
            });

            userFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedUser = parent.getItemAtPosition(position).toString();
                    filterPosts();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterPosts() {
        filteredPostList.clear();

        if (selectedUser.equals("Everyone")) {
            filteredPostList.addAll(postList); // Show all posts if "Everyone" is selected
        } else {
            for (Post post : postList) {
                if (post.getUsername().equalsIgnoreCase(selectedUser)) {
                    filteredPostList.add(post); // Add only posts by the selected user
                }
            }
        }
        mediaAdapter.notifyDataSetChanged(); // Refresh RecyclerView with filtered data
    }

    private void loadFromCache() {
        postList = new ArrayList<>(PostCache.getInstance().getPosts());
        filterPosts();
    }
}

