package dev.frilly.locket.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.function.BiConsumer;

import dev.frilly.locket.R;
import dev.frilly.locket.room.entities.UserProfile;

/**
 * A recycler view for a list of users within the friends list view.
 */
public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.Holder> {

    private final Context context;
    private final BiConsumer<UserProfile, FriendsListAdapter> destructiveAction;

    private List<UserProfile> dataSource;
    private BiConsumer<UserProfile, FriendsListAdapter> acceptAction;

    /**
     * Initializes a friends list adapter that displays "friends_recycler_row" layout in a list.
     *
     * @param context           The context that is responsible for this list adapter.
     * @param destructiveAction The action to do when the user clicks on the destructive action
     *                          button on the a row. Takes in the row number and the adapter.
     */
    public FriendsListAdapter(Context context,
                              BiConsumer<UserProfile, FriendsListAdapter> destructiveAction) {
        this.context = context;
        this.destructiveAction = destructiveAction;
    }

    /**
     * Sets the data source to the recycler view. This should reapply the changes to the recycler
     * view instantly.
     * <p>
     * The suppress annotation is to prevent Android Studio from complaining about "shouldn't be
     * using notifyDataSetChanged". I agree that we should use more specific events, like a row
     * changed, etc. But this sets the data source entirely, so that's not what I care about.
     *
     * @param dataSource the data source to set to
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setDataSource(List<UserProfile> dataSource) {
        this.dataSource = dataSource;
        this.notifyDataSetChanged();
    }

    public void setAcceptAction(BiConsumer<UserProfile, FriendsListAdapter> acceptAction) {
        this.acceptAction = acceptAction;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final var view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_recycler_row,
                parent, false);
        return new Holder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        final var profile = dataSource.get(position);

        Glide.with(context).load(profile.avatarUrl).circleCrop().into(holder.imageView);
        holder.usernameView.setText(profile.username);

        holder.descriptionView.setVisibility(View.VISIBLE);
        holder.acceptButton.setVisibility(View.GONE);
        holder.usernameView.setTextColor(context.getColor(R.color.white));
        holder.usernameView.setTypeface(Typeface.DEFAULT);

        switch (profile.friendState) {
            case FRIEND:
                holder.usernameView.setTextColor(context.getColor(R.color.gold));
                holder.usernameView.setTypeface(Typeface.DEFAULT_BOLD);
                holder.descriptionView.setVisibility(View.GONE);
                holder.destructiveButton.setImageResource(R.drawable.ic_person_remove);
                break;
            case SENT_REQUEST:
                holder.descriptionView.setText(R.string.text_description_sent);
                holder.destructiveButton.setImageResource(R.drawable.ic_cancel_send);
                break;
            case RECEIVED_REQUEST:
                holder.acceptButton.setVisibility(View.VISIBLE);
            default:
                holder.descriptionView.setText(R.string.text_description_received);
                holder.destructiveButton.setImageResource(R.drawable.ic_deny);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {

        private final FriendsListAdapter adapter;

        public ImageView imageView;
        public TextView usernameView;
        public TextView descriptionView;

        public ImageButton acceptButton;
        public ImageButton destructiveButton;

        public Holder(@NonNull View itemView, FriendsListAdapter adapter) {
            super(itemView);
            this.adapter = adapter;

            imageView = itemView.findViewById(R.id.image_profile);
            usernameView = itemView.findViewById(R.id.text_name);
            descriptionView = itemView.findViewById(R.id.text_description);
            acceptButton = itemView.findViewById(R.id.button_accept);
            destructiveButton = itemView.findViewById(R.id.button_delete);

            acceptButton.setOnClickListener(this::onAcceptClick);
            destructiveButton.setOnClickListener(this::onDestructiveClick);
        }

        private void onAcceptClick(final View view) {
            adapter.acceptAction.accept(adapter.dataSource.get(getBindingAdapterPosition()), adapter);
        }

        private void onDestructiveClick(final View view) {
            adapter.destructiveAction.accept(adapter.dataSource.get(getBindingAdapterPosition()), adapter);
        }

    }

}
