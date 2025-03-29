package dev.frilly.locket.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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

    private final boolean isFriendList;
    private final Context context;
    private final BiConsumer<Integer, FriendsListAdapter> destructiveAction;

    private List<UserProfile> dataSource;

    /**
     * Initializes a friends list adapter that displays "friends_recycler_row" layout in a list.
     *
     * @param context           The context that is responsible for this list adapter.
     * @param isFriendList      Whether to change the end-icon into a "friend-remove" icon or a normal
     *                          "close-delete" icon. See {@code ic_close.xml} and {@code
     *                          ic_person_remove.xml}.
     * @param destructiveAction The action to do when the user clicks on the destructive action
     *                          button on the a row. Takes in the row number and the adapter.
     */
    public FriendsListAdapter(Context context, boolean isFriendList,
                              BiConsumer<Integer, FriendsListAdapter> destructiveAction) {
        this.context = context;
        this.isFriendList = isFriendList;
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

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final var view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_recycler_row,
                parent, false);
        return new Holder(view, this, isFriendList);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Glide.with(context).load(dataSource.get(position).avatarUrl).circleCrop().into(holder.imageView);
        holder.usernameView.setText(dataSource.get(position).username);
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {

        private final FriendsListAdapter adapter;

        public ImageView imageView;
        public TextView usernameView;

        public Holder(@NonNull View itemView, FriendsListAdapter adapter, boolean isFriendList) {
            super(itemView);
            this.adapter = adapter;

            imageView = itemView.findViewById(R.id.image_profile);
            usernameView = itemView.findViewById(R.id.text_name);

            if (isFriendList) {
                final ImageButton destructiveButton = itemView.findViewById(R.id.button_delete);
                destructiveButton.setImageResource(R.drawable.ic_person_remove);
                destructiveButton.setOnClickListener(this::onDestructiveClick);
            }
        }

        private void onDestructiveClick(final View view) {
            adapter.destructiveAction.accept(getBindingAdapterPosition(), adapter);
        }

    }

}
