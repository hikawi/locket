package dev.frilly.locket.activities;
import dev.frilly.locket.R;
import dev.frilly.locket.adapter.ConfirmPostAdapter;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FriendsPostActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_post_screen);

        ImageView imageView = findViewById(R.id.friend_image_view);
        TextureView textureView = findViewById(R.id.friend_texture_view);
        TextView messageTextView = findViewById(R.id.message_confirm_input);
        TextView posterTextView = findViewById(R.id.poster);
        TextView postDateTextView = findViewById(R.id.post_date);

        ImageButton userAvatar = findViewById(R.id.user_avatar);
        ImageButton sendMessages = findViewById(R.id.message);
        ImageButton historyScreenBtn = findViewById(R.id.history_screen_btn);
        ImageButton cameraScreenBtn = findViewById(R.id.camera_screen_btn);
        ImageButton shareBtn = findViewById(R.id.share_button);

        // Get data from intent
        Intent intent = getIntent();
        if (intent != null) {
            String fileUrl = intent.getStringExtra("fileUrl");
            String message = intent.getStringExtra("message");
            String username = intent.getStringExtra("username");
            String postTime = intent.getStringExtra("postTime");

            // Debugging logs

            if (fileUrl == null || fileUrl.isEmpty()) {
                Toast.makeText(this, "File URL is empty!", Toast.LENGTH_SHORT).show();

            } else {
                // Load image using Glide only if URL is valid
                if (fileUrl.contains("image")) {
                    imageView.setVisibility(ImageView.VISIBLE);
                    textureView.setVisibility(TextView.GONE);
                    Glide.with(this).load(fileUrl).into(imageView);
                }

                // Play video using SurfaceTextureListener if URL is valid
                else if (fileUrl.contains("video")){
                    imageView.setVisibility(ImageView.GONE);
                    textureView.setVisibility(TextureView.VISIBLE);
                    textureView.setSurfaceTextureListener(new ConfirmPostAdapter.VideoTextureListener(this, Uri.parse(fileUrl), textureView));
                }
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
            Date nowDate = sdf.parse(sdf.format(new Date()));
            if (postDate != null && nowDate != null) {
                long diff = nowDate.getTime() - postDate.getTime();
                Log.d ("PostTime", diff + "|Now: " + nowDate + "|Post: " + postDate);
                long days = TimeUnit.MILLISECONDS.toDays(diff);
                if (days == 0) {
                    long hours = TimeUnit.MILLISECONDS.toHours(diff);
                    if (hours == 0) {
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                        if (minutes == 0) {
                            long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
                            return seconds + " seconds";
                        }
                        return minutes + " minutes";
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