package dev.frilly.locket.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;

import dev.frilly.locket.Authentication;
import dev.frilly.locket.Constants;
import dev.frilly.locket.room.entities.UserProfile;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Dedicated service class for fetching friends from the server.
 */
public final class FriendService {

    private static final FriendService instance = new FriendService();

    private FriendService() {
    }

    public static FriendService getInstance() {
        return instance;
    }

    /**
     * Fetches all friends and replaces everything inside the database.
     *
     * @param ctx The context to fetch with
     * @return A completion holder, which completes with the status whether the fetch was
     * successful or not.
     */
    public CompletableFuture<Boolean> fetchFriends(final Context ctx) {
        final var future = new CompletableFuture<Boolean>();

        // Delete all friends before fetching, we should only fetch once per app start.
        final var userDao = Constants.ROOM.userProfileDao();
        final var users = userDao.getProfiles();
        users.forEach(userDao::delete);

        // Keeps fetching new pages on the thread until all pages are fetched fully, recursively.
        try {
            final var req = new Request.Builder()
                    .url(Constants.BACKEND_URL + "friends")
                    .get()
                    .header("Authorization", "Bearer " + Authentication.getToken(ctx))
                    .build();

            Constants.HTTP_CLIENT.newCall(req).enqueue(new GetFriendsCallback(future, ctx));
        } catch (Exception e) {
            Log.e("FriendService", e.getMessage(), e);
            future.complete(false);
        }

        // Pre-emptively return the future so other code can instantly run without waiting
        // for the friends request to finish.
        return future;
    }

    private void insertFriends(final JSONArray array) throws JSONException {
        final var userDao = Constants.ROOM.userProfileDao();

        for (int i = 0; i < array.length(); i++) {
            final var friendObj = array.getJSONObject(i);
            final var profile = new UserProfile();
            profile.id = friendObj.getLong("id");
            profile.username = friendObj.getString("username");
            profile.email = friendObj.getString("email");
            profile.avatarUrl = friendObj.getString("avatar");

            final var bDayString = friendObj.optString("birthdate");
            if (bDayString != null) {
                profile.birthdate = LocalDateTime.parse(bDayString).toEpochSecond(ZoneOffset.UTC);
            }

            userDao.insert(new UserProfile());
        }
    }

    private final class GetFriendsCallback implements Callback {

        private final CompletableFuture<Boolean> future;
        private final Context context;

        public GetFriendsCallback(CompletableFuture<Boolean> future, Context context) {
            this.future = future;
            this.context = context;
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            Log.e("GetFriendsCallback", e.getMessage(), e);
            future.complete(false);
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            if (!response.isSuccessful()) {
                Log.e("GetFriendsCallback", "Getting friends return unknown status code");
                return;
            }

            try {
                final var obj = new JSONObject(response.body().string());
                Log.i("GetFriendsCallback", obj.toString());
                insertFriends(obj.getJSONArray("results"));

                // If there are more pages, fetch them too.
                // If not, then it's done.
                if (obj.getInt("totalPages") > obj.getInt("page") + 1) {
                    final var req = new Request.Builder()
                            .url(Constants.BACKEND_URL + "friends?page=" + (obj.getInt("page") + 1))
                            .header("Authorization", "Bearer " + Authentication.getToken(context))
                            .get()
                            .build();

                    Constants.HTTP_CLIENT.newCall(req).enqueue(new GetFriendsCallback(future,
                            context));
                } else {
                    future.complete(true);
                }
            } catch (Exception e) {
                Log.e("GetFriendsCallback", e.getMessage(), e);
                future.complete(false);
            }
        }

    }

}
