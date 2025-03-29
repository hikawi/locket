package dev.frilly.locket;

import dev.frilly.locket.room.LocalDatabase;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class Constants {

    public static final String KEY_COLlECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "locketPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";

    // Change the backend URL to your own local, or just
    // https://locket.frilly.dev/
    public static String BACKEND_URL = "https://locket.frilly.dev/";
    public static MediaType JSON = MediaType.get("application/json");
    public static OkHttpClient HTTP_CLIENT = new OkHttpClient();

    /**
     * The shared ROOM Database instance, this should be initialized by
     * the entry activity.
     */
    public static LocalDatabase ROOM;


}
