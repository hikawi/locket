package dev.frilly.locket;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class Constants {

    public static String BACKEND_URL = "https://locket.frilly.dev/";
    public static MediaType JSON = MediaType.get("application/json");
    public static OkHttpClient HTTP_CLIENT = new OkHttpClient();

}
