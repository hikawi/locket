package dev.frilly.locket;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A utility class for managing authentication tokens and user data securely.
 */
public class Authentication {

    private static final String TAG = "Authentication";
    private static final String SECURED_PREFS = "secured_prefs";
    private static final String USER_PREFS = "user_data";
    private static final String TOKEN_KEY = "token";
    private static final String USER_INFO_KEY = "user_info";
    private static final String USER_ID_KEY = "user_id";

    /**
     * Saves the authentication token securely.
     *
     * @param ctx   the application context
     * @param token the authentication token
     */
    public static void saveToken(final Context ctx, final String token) {
        try {
            MasterKey key = new MasterKey.Builder(ctx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences sharedPrefs = EncryptedSharedPreferences.create(
                    ctx,
                    SECURED_PREFS,
                    key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            sharedPrefs.edit().putString(TOKEN_KEY, token).apply();
        } catch (Exception exception) {
            Log.e(TAG, "Error saving token", exception);
        }
    }

    /**
     * Retrieves the stored authentication token.
     *
     * @param ctx the application context
     * @return the stored token, or an empty string if not found
     */
    public static String getToken(final Context ctx) {
        try {
            MasterKey key = new MasterKey.Builder(ctx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences sharedPrefs = EncryptedSharedPreferences.create(
                    ctx,
                    SECURED_PREFS,
                    key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            return sharedPrefs.getString(TOKEN_KEY, "");
        } catch (Exception exception) {
            Log.e(TAG, "Error retrieving token", exception);
            return "";
        }
    }

    /**
     * Removes the authentication token.
     *
     * @param ctx the application context
     */
    public static void unauthenticate(final Context ctx) {
        try {
            MasterKey key = new MasterKey.Builder(ctx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences sharedPrefs = EncryptedSharedPreferences.create(
                    ctx,
                    SECURED_PREFS,
                    key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            sharedPrefs.edit().remove(TOKEN_KEY).apply();
        } catch (Exception exception) {
            Log.e(TAG, "Error during unauthentication", exception);
        }
    }

    /**
     * Checks if a user is authenticated.
     *
     * @param ctx the application context
     * @return true if authenticated, false otherwise
     */
    @Deprecated
    public static boolean isAuthenticated(final Context ctx) {
        String token = getToken(ctx);
        return token != null && !token.isBlank();
    }

    /**
     * Sends a request to see if the user is properly authenticated.
     *
     * @param ctx the application context
     * @return a future that completes with true if authenticated, or false if not.
     */
    public static CompletableFuture<Boolean> checkAuthenticationStatus(final Context ctx) {
        final var token = getToken(ctx);
        if (token == null || token.isBlank()) {
            return CompletableFuture.completedFuture(false);
        }

        final var future = new CompletableFuture<Boolean>();
        final var req = new Request.Builder()
                .url(Constants.BACKEND_URL + "profiles")
                .header("Authorization", "Bearer " + token)
                .build();

        Constants.HTTP_CLIENT.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                future.complete(false);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.code() != 200) {
                    future.complete(false);
                    return;
                }

                final var body = response.body().string();
                try {
                    Authentication.saveUserData(ctx, new JSONObject(body));
                    future.complete(true);
                } catch (Exception e) {
                    future.complete(false);
                    e.printStackTrace();
                    Log.e("Authentication", "Failed authentication check.");
                }
            }
        });
        return future;
    }

    /**
     * Saves user data into SharedPreferences, including userId.
     *
     * @param ctx     the application context
     * @param userObj the user JSON object containing user details
     */
    public static void saveUserData(Context ctx, JSONObject userObj) {
        try {
            SharedPreferences sharedPrefs = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPrefs.edit();

            editor.putString(USER_INFO_KEY, userObj.toString());

            // Save userId separately for quick access
            if (userObj.has("id")) {
                editor.putString(USER_ID_KEY, userObj.getString("id"));
            }

            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving user data", e);
        }
    }

    /**
     * Retrieves the stored user data.
     *
     * @param ctx the application context
     * @return the user JSON object, or an empty object if not found
     */
    public static JSONObject getUserData(Context ctx) {
        try {
            SharedPreferences sharedPrefs = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
            String userData = sharedPrefs.getString(USER_INFO_KEY, "{}");
            return new JSONObject(userData);
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving user data", e);
            return new JSONObject();
        }
    }

    /**
     * Retrieves the stored userId.
     *
     * @param ctx the application context
     * @return the userId as a string, or an empty string if not found
     */
    public static String getUserId(Context ctx) {
        try {
            SharedPreferences sharedPrefs = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
            return sharedPrefs.getString(USER_ID_KEY, "");
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving user ID", e);
            return "";
        }
    }
}
