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
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

/**
 * The service instance that provides a front to handling requests and sending requests.
 */
public final class RequestService {

    private static final RequestService instance = new RequestService();

    private RequestService() {
    }

    public static RequestService getInstance() {
        return instance;
    }

    /**
     * Fetches all requests sent or received by the current user.
     *
     * @param ctx    The context to call on
     * @param myself Whether to fetch sent (TRUE) requests or received (FALSE) requests
     * @return the completable future that fills with the completion status
     */
    public CompletableFuture<Boolean> getRequests(final Context ctx, final boolean myself) {
        final var future = new CompletableFuture<Boolean>();

        // Delete all friends before fetching, we should only fetch once per app start.
        CompletableFuture.runAsync(() -> {
            final var userDao = Constants.ROOM.userProfileDao();
            userDao.deleteByFriendState(myself ? UserProfile.FriendState.SENT_REQUEST :
                    UserProfile.FriendState.RECEIVED_REQUEST);
        });

        final var token = Authentication.getToken(ctx);

        // Keeps fetching new pages on the thread until all pages are fetched fully, recursively.
        try {
            final var httpUrl = HttpUrl.parse(Constants.BACKEND_URL + "requests").newBuilder()
                    .addQueryParameter("myself", String.valueOf(myself))
                    .build();
            final var req = new Request.Builder()
                    .url(httpUrl)
                    .get()
                    .header("Authorization", "Bearer " + token)
                    .build();

            Constants.HTTP_CLIENT.newCall(req).enqueue(new GetRequestsCallback(future, token,
                    myself));
        } catch (Exception e) {
            Log.e("RequestService", e.getMessage(), e);
            future.complete(false);
        }

        return future;
    }

    private void insertRequests(final JSONArray array, final UserProfile.FriendState state,
                                final CompletableFuture<Boolean> future) {
        CompletableFuture.runAsync(() -> {
            try {
                final var userDao = Constants.ROOM.userProfileDao();
                Log.d("RequestService",
                        "Inserting " + array.length() + " objects with state " + state.name());

                for (int i = 0; i < array.length(); i++) {
                    final var friendObj = array.getJSONObject(i);
                    final var profile = new UserProfile();
                    profile.id = friendObj.getLong("id");
                    profile.username = friendObj.getString("username");
                    profile.email = friendObj.getString("email");
                    profile.avatarUrl = friendObj.getString("avatar");
                    profile.friendState = state;

                    if (!friendObj.isNull("birthdate")) {
                        final var bDayString = friendObj.getString("birthdate");
                        profile.birthdate = LocalDateTime.parse(bDayString).toEpochSecond(ZoneOffset.UTC);
                    }

                    userDao.insert(profile);
                }
            } catch (JSONException ex) {
                Log.e("RequestService", ex.getMessage(), ex);
            }

            if (future != null) {
                Log.d("RequestService", "Completing future for request service");
                future.complete(true);
            }
        });
    }

    private class GetRequestsCallback implements Callback {

        private final CompletableFuture<Boolean> future;
        private final String token;
        private final boolean myself;

        public GetRequestsCallback(final CompletableFuture<Boolean> future, final String token,
                                   final boolean myself) {
            this.future = future;
            this.token = token;
            this.myself = myself;
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            Log.e("GetRequestsCallback", e.getMessage(), e);
            future.complete(false);
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            if (!response.isSuccessful()) {
                Log.e("GetRequestsCallback", "Getting requests return unknown status code");
                return;
            }

            try {
                final var obj = new JSONObject(response.body().string());
                Log.i("GetRequestsCallback", obj.toString());
                final var state = myself ? UserProfile.FriendState.SENT_REQUEST :
                        UserProfile.FriendState.RECEIVED_REQUEST;

                final var hasNewPage = obj.getInt("totalPages") > obj.getInt("page") + 1;

                // If there are more pages, fetch them too.
                // If not, then it's done.
                if (hasNewPage) {
                    final var httpUrl =
                            HttpUrl.parse(Constants.BACKEND_URL + "requests").newBuilder()
                                    .addQueryParameter("page", String.valueOf(obj.getInt("page") + 1))
                                    .addQueryParameter("myself", String.valueOf(myself))
                                    .build();
                    final var req = new Request.Builder()
                            .url(httpUrl)
                            .get()
                            .header("Authorization", "Bearer " + token)
                            .build();

                    Constants.HTTP_CLIENT.newCall(req).enqueue(new GetRequestsCallback(future, token,
                            myself));
                }

                insertRequests(obj.getJSONArray("results"), state, hasNewPage ? null : future);
            } catch (Exception e) {
                Log.e("GetRequestsCallback", e.getMessage(), e);
                future.complete(false);
            }
        }

    }

}
