package dev.frilly.locket;

import android.content.Context;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import org.json.JSONObject;

/**
 * Class to check for authentication token.
 */
public class Authentication {

    /**
     * Saves the token.
     *
     * @param ctx   the context
     * @param token the token
     */
    public static void saveToken(final Context ctx, final String token) {
        try {
            final var key = new MasterKey.Builder(ctx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            final var sharedPrefs = EncryptedSharedPreferences.create(
                    ctx,
                    "secured_prefs",
                    key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            sharedPrefs.edit().putString("token", token).apply();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Gets the token, if available.
     *
     * @param ctx the context
     * @return the token
     */
    public static String getToken(final Context ctx) {
        try {
            final var key = new MasterKey.Builder(ctx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            final var sharedPrefs = EncryptedSharedPreferences.create(
                    ctx,
                    "secured_prefs",
                    key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            return sharedPrefs.getString("token", "");
        } catch (Exception exception) {
            exception.printStackTrace();
            return "";
        }
    }

    /**
     * Removes the token field.
     *
     * @param ctx the context
     */
    public static void unauthenticate(final Context ctx) {
        try {
            final var key = new MasterKey.Builder(ctx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            final var sharedPrefs = EncryptedSharedPreferences.create(
                    ctx,
                    "secured_prefs",
                    key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            sharedPrefs.edit().remove("token").apply();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Quick authentication check.
     *
     * @param ctx the context
     * @return if it is authenticated
     */
    public static boolean isAuthenticated(final Context ctx) {
        final var token = getToken(ctx);
        return token != null && !token.isBlank();
    }

    // Lưu User Data vào SharedPreferences
    public static void saveUserData(Context ctx, JSONObject userObj) {
        try {
            final var sharedPrefs = ctx.getSharedPreferences("user_data", Context.MODE_PRIVATE);
            sharedPrefs.edit()
                    .putString("user_info", userObj.toString())
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Lấy User Data từ SharedPreferences
    public static JSONObject getUserData(Context ctx) {
        try {
            final var sharedPrefs = ctx.getSharedPreferences("user_data", Context.MODE_PRIVATE);
            String userData = sharedPrefs.getString("user_info", "{}");
            return new JSONObject(userData);
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

}
