package dev.frilly.locket.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        holder.imageView.setImageURI(Uri.parse(images.get(position / 3))); // Divide by 3 to match images

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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        EditText messageInput;
        LinearLayout timeLayout, locationLayout;
        TextView timeTextView, locationTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image_view);
            messageInput = itemView.findViewById(R.id.item_message_input);

            timeLayout = itemView.findViewById(R.id.time_layout);
            timeTextView = itemView.findViewById(R.id.item_time);

            locationLayout = itemView.findViewById(R.id.location_layout);
            locationTextView = itemView.findViewById(R.id.item_location);
        }
    }
}