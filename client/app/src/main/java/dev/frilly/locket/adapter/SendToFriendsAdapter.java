package dev.frilly.locket.adapter;

import dev.frilly.locket.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.frilly.locket.room.entities.UserProfile;

public class SendToFriendsAdapter extends RecyclerView.Adapter<SendToFriendsAdapter.ViewHolder> {

    private final Context context;
    private final List<UserProfile> friends;
    private final List<Boolean> selectionStates;
    private int selectedAllIndex = 0;

    public SendToFriendsAdapter(Context context, List<UserProfile> friendsList) {
        this.context = context;
        this.friends = new ArrayList<>();
        this.friends.add(null);
        this.friends.addAll(friendsList);

        this.selectionStates = new ArrayList<>(Collections.nCopies(this.friends.size(), false));
        selectionStates.set(0, true); // Default select "All"
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_avatar, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        boolean isSelected = selectionStates.get(position);

        if (position == 0 || friends.get(position) == null) {
            // Special "All" case
            holder.usernameText.setText("All");
            holder.avatarImage.setImageResource(R.drawable.all_users);
        } else {
            UserProfile user = friends.get(position);
            holder.usernameText.setText(user.username);

            Glide.with(context)
                    .load(user.avatarUrl)
                    .placeholder(R.drawable.account_circle_16dp)
                    .circleCrop()
                    .into(holder.avatarImage);
        }

        // Highlight effect
        holder.itemView.setAlpha(isSelected ? 1.0f : 0.5f);

        holder.itemView.setOnClickListener(v -> {
            if (position == 0) {
                for (int i = 0; i < selectionStates.size(); i++) {
                    selectionStates.set(i, i == 0); // Only "All" selected
                }
            } else {
                selectionStates.set(0, false); // Deselect "All"
                selectionStates.set(position, !selectionStates.get(position));
            }
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public List<Long> getSelectedFriendIds() {
        List<Long> ids = new ArrayList<>();
        if (selectionStates.get(0)) {
            for (int i = 1; i < friends.size(); i++) {
                ids.add(friends.get(i).id);
            }
        } else {
            for (int i = 1; i < friends.size(); i++) {
                if (selectionStates.get(i)) {
                    ids.add(friends.get(i).id);
                }
            }
        }
        return ids;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImage;
        TextView usernameText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.avatar_image);
            usernameText = itemView.findViewById(R.id.username_text);
        }
    }
}
