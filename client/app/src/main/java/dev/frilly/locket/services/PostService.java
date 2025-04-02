package dev.frilly.locket.services;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import dev.frilly.locket.Authentication;
import dev.frilly.locket.Constants;
import dev.frilly.locket.activities.CameraActivity;
import dev.frilly.locket.model.Post;
import dev.frilly.locket.model.PostCache;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public final class PostService {

    private static final PostService instance = new PostService();

    private PostService() {
    }

    public static PostService getInstance() { return instance; }

    public CompletableFuture<Boolean> fetchPostsOnce(final Context ctx) {
        final CompletableFuture<Boolean> future = new CompletableFuture<>();

        // If already cached, no need to fetch again
        if (!PostCache.getInstance().getPosts().isEmpty()) {
            future.complete(true);
            return future;
        }

        String fromDate = LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String toDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Request request = new Request.Builder()
                .url(Constants.BACKEND_URL + "posts?since=" + fromDate + "&until=" + toDate)
                .get()
                .addHeader("Authorization", "Bearer " + Authentication.getToken(ctx))
                .build();

        Constants.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("PostService", "Failed to fetch posts", e);
                future.complete(false);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("PostService", "Unsuccessful response: " + response.code());
                    future.complete(false);
                    return;
                }

                try {
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    JSONArray results = json.getJSONArray("results");

                    List<Post> posts = new ArrayList<>();
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject postObject = results.getJSONObject(i);
                        JSONObject poster = postObject.getJSONObject("poster");

                        posts.add(new Post(
                                postObject.getString("imageLink"),
                                poster.getString("username"),
                                postObject.getString("message"),
                                postObject.getString("time")
                        ));
                    }

                    // Cache the posts once parsed
                    PostCache.getInstance().setPosts(posts);
                    future.complete(true);

                } catch (JSONException e) {
                    Log.e("PostService", "Failed to parse JSON", e);
                    future.complete(false);
                }
            }
        });

        return future;
    }
}
