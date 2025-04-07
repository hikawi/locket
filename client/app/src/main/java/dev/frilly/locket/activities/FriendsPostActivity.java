package dev.frilly.locket.activities;
import dev.frilly.locket.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FriendsPostActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView messageTextView;
    private TextView posterTextView;
    private TextView postDateTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_post_screen);

        imageView = findViewById(R.id.imageView);
        messageTextView = findViewById(R.id.message_confirm_input);
        posterTextView = findViewById(R.id.poster);
        postDateTextView = findViewById(R.id.post_date);

        ImageButton userAvatar = findViewById(R.id.user_avatar);
        ImageButton sendMessages = findViewById(R.id.message);
        ImageButton historyScreenBtn = findViewById(R.id.history_screen_btn);
        ImageButton cameraScreenBtn = findViewById(R.id.camera_screen_btn);
        ImageButton shareBtn = findViewById(R.id.share_button);

        // Get data from intent
        Intent intent = getIntent();
        if (intent != null) {
            String imageUrl = intent.getStringExtra("imageUrl");
            String message = intent.getStringExtra("message");
            String username = intent.getStringExtra("username");
            String postTime = intent.getStringExtra("postTime");

            // Debugging logs
            if (imageUrl == null || imageUrl.isEmpty()) {
                Toast.makeText(this, "Image URL is empty!", Toast.LENGTH_SHORT).show();
            } else {
                // Load image using Glide only if URL is valid
                Glide.with(this).load(imageUrl).into(imageView);
            }

            // Set text values
            messageTextView.setText(message != null ? message : "No message");
            posterTextView.setText(username != null ? username : "Unknown");
            postDateTextView.setText(postTime != null ? calculateTimeDifference(postTime) : "Unknown date");
        }

        userAvatar.setOnClickListener(v -> {
            final var profileIntent = new Intent(FriendsPostActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        });

        sendMessages.setOnClickListener(v -> {
            final var chatIntent = new Intent(FriendsPostActivity.this, RecentChatsActivity.class);
            startActivity(chatIntent);
        });

        historyScreenBtn.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        cameraScreenBtn.setOnClickListener(v -> {
            setResult(RESULT_OK); // signal to close HistoryActivity
            finish(); // close this screen
        });

        shareBtn.setOnClickListener(v -> {
            String imageUrl = intent.getStringExtra("imageUrl");
            ShareBottomSheetDialog dialog = new ShareBottomSheetDialog(imageUrl);
            dialog.show(getSupportFragmentManager(), "ShareDialog");
        });
    }

    private String calculateTimeDifference(String postTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date postDate = sdf.parse(postTime);
            if (postDate != null) {
                long diff = new Date().getTime() - postDate.getTime();
                long days = TimeUnit.MILLISECONDS.toDays(diff);

                if (days == 0) {
                    long hours = TimeUnit.MILLISECONDS.toHours(diff);
                    if (hours == 0) {
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                        if (minutes == 0) {
                            long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
                            return seconds + " seconds";
                        }
                        return minutes + " hours";
                    }
                    return hours + " hours";
                }
                return days + " days";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
}

