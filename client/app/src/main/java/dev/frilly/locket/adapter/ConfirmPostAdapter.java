package dev.frilly.locket.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import dev.frilly.locket.R;

public class ConfirmPostAdapter extends RecyclerView.Adapter<ConfirmPostAdapter.ViewHolder> {
    private Context context;
    private List<String> images;
    private String location;
    private HashMap<Integer, String> messages = new HashMap<>(); // Store messages for each page

    public ConfirmPostAdapter(Context context, List<String> images, String location) {
        this.context = context;
        this.images = images;
        this.location = location;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateLocation(String userLocation) {
        this.location = userLocation;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_confirm_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int pageType = position % 3; // 0 = Message, 1 = Time, 2 = Location
        String filePath = images.get(position / 3);
        boolean isVideo = filePath.endsWith(".mp4") || filePath.endsWith(".mkv");

        // Check if user chooses image or video to show on view
        if (isVideo) {
            holder.imageView.setVisibility(View.GONE);
            holder.textureView.setVisibility(View.VISIBLE);
            holder.textureView.setSurfaceTextureListener(new VideoTextureListener(context, Uri.parse(filePath), holder.textureView));
        } else {
            holder.textureView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            holder.imageView.setImageURI(Uri.parse(filePath));
        }

        if (pageType == 0) {
            // Show EditText for message
            holder.messageInput.setVisibility(View.VISIBLE);
            holder.timeLayout.setVisibility(View.GONE);
            holder.locationLayout.setVisibility(View.GONE);

            // Restore previous message if exists
            if (messages.containsKey(position)) {
                holder.messageInput.setText(messages.get(position));
            }

            // Save message when user types
            holder.messageInput.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    messages.put(position, holder.messageInput.getText().toString());
                }
            });

        } else if (pageType == 1) {
            // Show Time Layout
            holder.messageInput.setVisibility(View.GONE);
            holder.timeLayout.setVisibility(View.VISIBLE);
            holder.locationLayout.setVisibility(View.GONE);

            // Store the time when the page is first created
            if (!messages.containsKey(position)) {
                String currentTime = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(new Date());
                messages.put(position, currentTime);
            }
            holder.timeTextView.setText(messages.get(position));

        } else if (pageType == 2) {
            // Show Location Layout
            holder.messageInput.setVisibility(View.GONE);
            holder.timeLayout.setVisibility(View.GONE);
            holder.locationLayout.setVisibility(View.VISIBLE);

            // Store location when available
            if (!messages.containsKey(position)) {
                messages.put(position, location);
            }
            holder.locationTextView.setText(messages.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return images.size() * 3; // Each image has 3 pages (Message, Time, Location)
    }

    public HashMap<Integer, String> getMessages() {
        return messages;
    }

    public String getMessageItem(int position) {
        return messages.getOrDefault(position, "No Text");
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextureView textureView;
        EditText messageInput;
        LinearLayout timeLayout, locationLayout;
        TextView timeTextView, locationTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image_view);
            textureView = itemView.findViewById(R.id.item_texture_view);
            messageInput = itemView.findViewById(R.id.item_message_input);

            timeLayout = itemView.findViewById(R.id.time_layout);
            timeTextView = itemView.findViewById(R.id.item_time);

            locationLayout = itemView.findViewById(R.id.location_layout);
            locationTextView = itemView.findViewById(R.id.item_location);
        }
    }

    public static class ScaledVideoView extends VideoView {
        public ScaledVideoView(Context context) {
            super(context);
        }

        public ScaledVideoView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public ScaledVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            setMeasuredDimension(width, height);
        }
    }

    // Create listener class for playing video and also adjust video to center and fullscreen like image
    public static class VideoTextureListener implements TextureView.SurfaceTextureListener {
        private final Context context;
        private final Uri videoUri;
        private final TextureView textureView;

        public VideoTextureListener(Context context, Uri videoUri, TextureView textureView) {
            this.context = context;
            this.videoUri = videoUri;
            this.textureView = textureView;
        }

        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
            try {
                Surface surface = new Surface(surfaceTexture);
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(context, videoUri);
                mediaPlayer.setSurface(surface);
                mediaPlayer.setLooping(true);
                mediaPlayer.setOnPreparedListener(mp -> {
                    adjustTextureScale(mp.getVideoWidth(), mp.getVideoHeight());
                    mp.start();
                });
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                Log.e("VideoTextureListener", "Video error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void adjustTextureScale(int videoWidth, int videoHeight) {
            float viewWidth = textureView.getWidth();
            float viewHeight = textureView.getHeight();

            float videoRatio = (float) videoWidth / videoHeight;
            float viewRatio = viewWidth / viewHeight;

            float scaleX = 1f;
            float scaleY = 1f;

            if (videoRatio > viewRatio) {
                scaleX = videoRatio / viewRatio;
            } else {
                scaleY = viewRatio / videoRatio;
            }

            Matrix matrix = new Matrix();
            matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f);
            textureView.setTransform(matrix);
        }

        @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}
        @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) { return true; }
        @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
    }
}