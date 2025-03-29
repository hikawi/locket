package dev.frilly.locket.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import dev.frilly.locket.Authentication;
import dev.frilly.locket.Constants;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Represents the service that specializes in dealing with fetching user profiles.
 * <p>
 * This is mainly being used for fetching own profile, instead of others.
 */
public final class ProfileService {

    private static final ProfileService instance = new ProfileService();

    private ProfileService() {
        // Left blank.
    }

    public static ProfileService getInstance() {
        return instance;
    }

    /**
     * Fetch the user's profile and save into the context, if possible.
     *
     * @return The completable future instance, when completed, it will be filled
     * with either true or false, marking the success of the fetch request.
     */
    public CompletableFuture<Boolean> fetch(final Context ctx) {
        try {
            final var req = new Request.Builder()
                    .url(Constants.BACKEND_URL + "profiles")
                    .header("Authorization", "Bearer " + Authentication.getToken(ctx))
                    .get()
                    .build();

            final var future = new CompletableFuture<Boolean>();
            Constants.HTTP_CLIENT.newCall(req).enqueue(new GetSelfProfileCallback(future, ctx));
            return future;
        } catch (Exception e) {
            Log.e("ProfileService", e.getMessage(), e);
            return CompletableFuture.completedFuture(false);
        }
    }

    private static class GetSelfProfileCallback implements Callback {

        private final CompletableFuture<Boolean> future;
        private final Context context;

        public GetSelfProfileCallback(CompletableFuture<Boolean> future, Context ctx) {
            this.future = future;
            this.context = ctx;
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            Log.e("GetSelfProfileCallback", e.getMessage(), e);
            future.complete(false);
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            if (!response.isSuccessful()) {
                future.complete(false);
                return;
            }

            // Save into Authentication.
            try {
                final var body = new JSONObject(response.body().string());
                Authentication.saveUserData(context, body);
                Log.i("GetSelfProfilesCallback", body.toString());
                future.complete(true);
            } catch (JSONException e) {
                Log.e("GetSelfProfilesCallback", e.getMessage(), e);
                future.complete(false);
            }
        }

    }

}
