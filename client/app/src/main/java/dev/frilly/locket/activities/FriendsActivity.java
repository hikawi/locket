package dev.frilly.locket.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import dev.frilly.locket.Authentication;
import dev.frilly.locket.Constants;
import dev.frilly.locket.R;
import dev.frilly.locket.adapter.FriendsListAdapter;
import dev.frilly.locket.room.entities.UserProfile;
import dev.frilly.locket.services.RequestService;
import dev.frilly.locket.utils.AndroidUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * The activity responsible for handling sending friend requests and displaying
 * friends, sent requests and accept requests
 */
public class FriendsActivity extends AppCompatActivity {

    private List<UserProfile> cacheProfiles = new ArrayList<>();
    private ImageButton backButton;

    // The stuff for sending a friend request.
    private TextView usernameField;
    private Button sendButton;

    // The section contains a recycler view, the decorative view and more button
    // that appears if the limit < max Length.
    private RecyclerView friendsRecyclerView;
    private View friendsDecorativeView;
    private Button friendsMoreButton;
    private int friendsLimit = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_screen);
        AndroidUtil.applyInsets(this, R.id.layout_outer);

        backButton = findViewById(R.id.button_back);

        usernameField = findViewById(R.id.field_username);
        sendButton = findViewById(R.id.button_send_request);

        friendsRecyclerView = findViewById(R.id.recycler_friends);
        friendsDecorativeView = findViewById(R.id.view_friends_decorative);
        friendsMoreButton = findViewById(R.id.button_friends_more);

        sendButton.setOnClickListener(v -> onSendButtonClick(usernameField.getText().toString()));
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        friendsMoreButton.setOnClickListener(v -> {
            friendsLimit += 1;
            updateDataSource();
        });

        setupFriendsRecyclerView();
        fetchData();
    }

    private void onSendButtonClick(final String username) {
        try {
            final var body = new JSONObject();
            body.put("username", username);

            final var req = new Request.Builder()
                    .url(Constants.BACKEND_URL + "requests")
                    .header("Authorization", "Bearer " + Authentication.getToken(this))
                    .post(RequestBody.create(body.toString(), Constants.JSON))
                    .build();

            Constants.HTTP_CLIENT.newCall(req).enqueue(new SendRequestCallback(username));
        } catch (Exception e) {
            Log.e("FriendsActivity", e.getMessage(), e);
        }
    }

    private void updateDataSource() {
        Log.d("FriendsActivity", "Updating data source with " + cacheProfiles.size() + " cache");

        // Set the decorations of the button "more" to be visible if the limit
        // is smaller than the actual profiles list.
        if (cacheProfiles.size() > friendsLimit) {
            friendsDecorativeView.setVisibility(View.VISIBLE);
            friendsMoreButton.setVisibility(View.VISIBLE);
        } else {
            friendsDecorativeView.setVisibility(View.GONE);
            friendsMoreButton.setVisibility(View.GONE);
        }

        final var adapter = (FriendsListAdapter) friendsRecyclerView.getAdapter();
        final var list = cacheProfiles.stream().sorted().limit(friendsLimit).collect(Collectors.toList());
        adapter.setDataSource(list);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchData() {
        CompletableFuture.supplyAsync(() -> {
            final var dao = Constants.ROOM.userProfileDao();
            return dao.getProfiles();
        }).thenAccept(liveData -> runOnUiThread(() -> liveData.observe(this, list -> {
            cacheProfiles = list;
            updateDataSource();
        })));

        RequestService.getInstance().getRequests(this, true).thenAccept(status -> {
            Log.d("FriendsActivity", "Fetched sent requests");
        });

        RequestService.getInstance().getRequests(this, false).thenAccept(status -> {
            Log.d("FriendsActivity", "Fetched received requests");
        });
    }

    private void setupFriendsRecyclerView() {
        // Setup adapter for friends list.
        final var adapter = new FriendsListAdapter(this, (item, adap) -> {
            if (Objects.requireNonNull(item.friendState) == UserProfile.FriendState.FRIEND) {
                removeFriend(item);
            } else {
                removeRequest(item);
            }
        });
        adapter.setAcceptAction((item, adap) -> {
            if (item.friendState != UserProfile.FriendState.RECEIVED_REQUEST) {
                return;
            }

            Log.d("FriendsActivity", "Trying to accept from " + item.username);
            onSendButtonClick(item.username);
        });

        friendsRecyclerView.setAdapter(adapter);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false));
        adapter.setDataSource(cacheProfiles);
    }

    private void removeRequest(final UserProfile profile) {
        Log.d("FriendsActivity", "Trying to remove request " + profile.username);
        try {
            final var body = new JSONObject();
            body.put("username", profile.username);

            final var req = new Request.Builder()
                    .url(Constants.BACKEND_URL + "requests")
                    .header("Authorization", "Bearer " + Authentication.getToken(this))
                    .delete(RequestBody.create(body.toString(), Constants.JSON))
                    .build();

            Constants.HTTP_CLIENT.newCall(req).enqueue(new DeleteFriendCallback(profile));
        } catch (Exception e) {
            Log.e("FriendsActivity", "Failed trying to remove friend", e);
        }
    }

    private void removeFriend(final UserProfile profile) {
        Log.d("FriendsActivity", "Trying to remove friend " + profile.username);
        try {
            final var body = new JSONObject();
            body.put("id", profile.id);

            final var req = new Request.Builder()
                    .url(Constants.BACKEND_URL + "friends")
                    .header("Authorization", "Bearer " + Authentication.getToken(this))
                    .delete(RequestBody.create(body.toString(), Constants.JSON))
                    .build();

            Constants.HTTP_CLIENT.newCall(req).enqueue(new DeleteFriendCallback(profile));
        } catch (Exception e) {
            Log.e("FriendsActivity", "Failed trying to remove friend", e);
        }
    }

    private final class DeleteFriendCallback implements Callback {

        private final UserProfile profile;

        private DeleteFriendCallback(final UserProfile profile) {
            this.profile = profile;
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            Log.e("DeleteFriendCallback", "Error in request", e);
            AndroidUtil.showToast(FriendsActivity.this, getString(R.string.error_unknown));
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            Log.d("DeleteFriendCallback", "Response returned with " + response.code());
            if (response.code() != 200) {
                return;
            }

            Log.d("DeleteFriendsCallback", "Deleting profile from DAO");
            Constants.ROOM.userProfileDao().delete(profile);
        }

    }

    /**
     * The callback for posting a request. This should handler both ACCEPTING the request and
     * SENDING a new one.
     */
    private final class SendRequestCallback implements Callback {

        private final String username;

        private SendRequestCallback(String username) {
            this.username = username;
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            Log.e("SendRequestCallback", e.getMessage(), e);
            runOnUiThread(() -> AndroidUtil.showToast(FriendsActivity.this, getString(R.string.error_unknown)));
        }

        private void handleAccept(final Response response) {
            Log.d("SendRequestCallback", "Accepting from " + username);
            final var profile = Constants.ROOM.userProfileDao().getByUsername(username);
            if (profile == null)
                return;
            profile.friendState = UserProfile.FriendState.FRIEND;
            Constants.ROOM.userProfileDao().update(profile);
        }

        private void handleNewSend(final Response response) {
            Log.d("SendRequestCallback", "Sending new to " + username);
            try {
                final var body = new JSONObject(response.body().string());
                final var profile = new UserProfile();
                profile.id = body.getLong("id");
                profile.username = body.getString("username");
                profile.email = body.getString("email");
                profile.friendState = UserProfile.FriendState.SENT_REQUEST;

                if (!body.isNull("avatar")) {
                    profile.avatarUrl = body.getString("avatar");
                }

                if (!body.isNull("birthdate")) {
                    profile.birthdate = AndroidUtil.dateStringToMillis(body.getString("birthdate"));
                }

                Constants.ROOM.userProfileDao().insert(profile);
            } catch (Exception e) {
                Log.e("SendRequestCallback", e.getMessage(), e);
            }
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            Log.d("SEndRequestCallback", "Callback exited with " + response.code());
            switch (response.code()) {
                case 400: // Bad username
                    runOnUiThread(() -> AndroidUtil.showToast(FriendsActivity.this,
                            getString(R.string.error_username_invalid)));
                    break;
                case 404: // Unknown user
                    runOnUiThread(() -> AndroidUtil.showToast(FriendsActivity.this,
                            getString(R.string.error_username_not_found)));
                    break;
                case 409: // Already sent
                    runOnUiThread(() -> AndroidUtil.showToast(FriendsActivity.this,
                            getString(R.string.error_already_requested)));
                    break;
                case 403: // Sent to self
                    runOnUiThread(() -> AndroidUtil.showToast(FriendsActivity.this,
                            getString(R.string.error_self)));
                    break;
                case 204: // Accepted a request.
                    handleAccept(response);
                    break;
                case 200: // Sent a new request to another user
                    handleNewSend(response);
                    break;
                default:
                    runOnUiThread(() -> AndroidUtil.showToast(FriendsActivity.this,
                            getString(R.string.error_unknown)));
                    break;
            }
        }

    }

}
