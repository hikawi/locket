package dev.frilly.locket.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import dev.frilly.locket.Constants;
import dev.frilly.locket.R;
import dev.frilly.locket.adapter.FriendsListAdapter;
import dev.frilly.locket.room.entities.UserProfile;
import dev.frilly.locket.services.RequestService;
import dev.frilly.locket.utils.AndroidUtil;

/**
 * The activity responsible for handling sending friend requests and displaying
 * friends, sent requests and accept requests
 */
public class FriendsActivity extends AppCompatActivity {

    private final List<UserProfile> friendsDataSource = new CopyOnWriteArrayList<>();

    private ImageButton backButton;

    // The section contains a recycler view, the decorative view and more button
    // that appears if the limit < max Length.
    private RecyclerView friendsRecyclerView;
    private View friendsDecorativeView;
    private Button friendsMoreButton;
    private int friendsLimit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_screen);
        AndroidUtil.applyInsets(this, R.id.layout_outer);

        backButton = findViewById(R.id.button_back);

        friendsRecyclerView = findViewById(R.id.recycler_friends);
        friendsDecorativeView = findViewById(R.id.view_friends_decorative);
        friendsMoreButton = findViewById(R.id.button_friends_more);

        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        setupFriendsRecyclerView();

        // Fetch data
        friendsDataSource.clear();
        fetchData();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void notifyDataSet() {
        runOnUiThread(() -> Objects.requireNonNull(friendsRecyclerView.getAdapter()).notifyDataSetChanged());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchData() {
        CompletableFuture.runAsync(() -> {
            final var dao = Constants.ROOM.userProfileDao();
            final var liveList = dao.getProfiles();
            liveList.observe(this, userProfiles -> {
                runOnUiThread(() -> {
                    final var adapter = (FriendsListAdapter) friendsRecyclerView.getAdapter();
                    adapter.setDataSource(userProfiles);
                });
            });
        });

        // Friends are fetched on the camera activity screen already.
        // So we apply that, and reapply the data source later.
        CompletableFuture.runAsync(() -> {
            friendsDataSource.addAll(
                    Constants.ROOM.userProfileDao().getProfiles(UserProfile.FriendState.FRIEND));
            friendsDataSource.sort(UserProfile::compareTo);
            notifyDataSet();
        });

        // We only need to fetch the requests only. We fetch our sent requests here.
        // There are two blocks but the two fetch requests here and below run at the same time.
        RequestService.getInstance().getRequests(this, true).thenAccept(status -> {
            synchronized (friendsDataSource) {
                if (status) {
                    Log.d("FriendsActivity", "Successfully fetched all sent requests");
                    friendsDataSource.addAll(Constants.ROOM.userProfileDao().getProfiles(UserProfile.FriendState.SENT_REQUEST));
                    friendsDataSource.sort(UserProfile::compareTo);
                    notifyDataSet();
                } else {
                    AndroidUtil.showToast(this, getString(R.string.error_unknown));
                }
            }
        });

        // Then we fetch the requests the user received.
        RequestService.getInstance().getRequests(this, false).thenAccept(status -> {
            synchronized (friendsDataSource) {
                if (status) {
                    Log.d("FriendsActivity", "Successfully fetched all received requests");
                    friendsDataSource.addAll(Constants.ROOM.userProfileDao().getProfiles(UserProfile.FriendState.RECEIVED_REQUEST));
                    friendsDataSource.sort(UserProfile::compareTo);
                    notifyDataSet();
                } else {
                    AndroidUtil.showToast(this, getString(R.string.error_unknown));
                }
            }
        });
    }

    private void removeUserProfile(int index) {

    }

    private void setupFriendsRecyclerView() {
        // Setup adapter for friends list.
        final var adapter = new FriendsListAdapter(this, (pos, a) -> {
            removeUserProfile(pos);
            a.notifyItemRemoved(pos);
        });
        friendsRecyclerView.setAdapter(adapter);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false));
        adapter.setDataSource(friendsDataSource);
    }

}
