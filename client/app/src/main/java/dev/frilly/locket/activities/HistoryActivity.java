package dev.frilly.locket.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import dev.frilly.locket.Authentication;
import dev.frilly.locket.Constants;
import dev.frilly.locket.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView imageRecyclerView;
    private MediaAdapter mediaAdapter;
    private ArrayList<PostData> postList = new ArrayList<>();
    private ArrayList<PostData> filteredPostList = new ArrayList<>();
    private Context context;
    private Spinner userFilterSpinner;
    private String selectedUser = "Everyone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_screen);

        imageRecyclerView = findViewById(R.id.image_recycler_view);
        userFilterSpinner = findViewById(R.id.user_filter_spinner);
        context = getApplicationContext();

        // Set Grid Layout with 3 columns
        imageRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        mediaAdapter = new MediaAdapter(filteredPostList, context, position -> {
            PostData post = filteredPostList.get(position);

            // Debug log to check if correct URL is passed
            if (post.getImageUrl() == null || post.getImageUrl().isEmpty()) {
                Toast.makeText(context, "Error: Image URL is empty!", Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(HistoryActivity.this, FriendsPostActivity.class);
            intent.putExtra("imageUrl", post.getImageUrl());
            intent.putExtra("message", post.getMessage());
            intent.putExtra("username", post.getUsername());
            intent.putExtra("postTime", post.getPostTime());
            startActivity(intent);
        });


        imageRecyclerView.setAdapter(mediaAdapter);

        setupSpinner();
        fetchImagesFromBackend();
    }

    private void setupSpinner() {
        String[] users = {"Everyone", "nthung", "nthung01", "nthung02"};

        // Use android.R.layout.simple_spinner_item instead of simple_spinner_dropdown_item
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, users);
        adapter.setDropDownViewResource(R.layout.spinner_drop_item);

        userFilterSpinner.setAdapter(adapter);

        // Ensure "Everyone" is selected by default AFTER setting the adapter
        userFilterSpinner.post(() -> userFilterSpinner.setSelection(0, false));

        userFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUser = parent.getItemAtPosition(position).toString();
                filterPosts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void filterPosts() {
        filteredPostList.clear();

        if (selectedUser.equals("Everyone")) {
            filteredPostList.addAll(postList); // Show all posts if "Everyone" is selected
        } else {
            for (PostData post : postList) {
                if (post.getUsername().equalsIgnoreCase(selectedUser)) {
                    filteredPostList.add(post); // Add only posts by the selected user
                }
            }
        }

        mediaAdapter.notifyDataSetChanged(); // Refresh RecyclerView with filtered data
    }


    private void fetchImagesFromBackend() {
        String fromDate = LocalDateTime.now().minusDays(7)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String toDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        try {
            Request request = new Request.Builder()
                    .url(Constants.BACKEND_URL + "posts?from=" + fromDate + "&to=" + toDate)
                    .get()
                    .addHeader("Authorization", "Bearer " + Authentication.getToken(this))
                    .build();

            Constants.HTTP_CLIENT.newCall(request).enqueue(new GetHistoryCallback());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class GetHistoryCallback implements Callback {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(context, "Failed to fetch posts", Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            if (response.isSuccessful() && response.body() != null) {
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    JSONArray resultsArray = jsonResponse.getJSONArray("results");

                    postList.clear();

                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject postObject = resultsArray.getJSONObject(i);
                        JSONObject poster = postObject.getJSONObject("poster");

                        String username = poster.getString("username");
                        String imageUrl = postObject.getString("image");
                        String message = postObject.getString("message");
                        String postTime = postObject.getString("time");

                        postList.add(new PostData(imageUrl, username, message, postTime));
                        filterPosts();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                runOnUiThread(() -> Toast.makeText(context, "Error fetching posts", Toast.LENGTH_SHORT).show());
            }
        }
    }
}

