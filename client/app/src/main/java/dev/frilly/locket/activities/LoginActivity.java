package dev.frilly.locket.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import dev.frilly.locket.Authentication;
import dev.frilly.locket.Constants;
import dev.frilly.locket.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * The activity for the login screen.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText usernameField;
    private EditText passwordField;

    private TextView textError;
    private Button loginButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        usernameField = findViewById(R.id.field_username);
        passwordField = findViewById(R.id.field_password);
        textError = findViewById(R.id.text_error);
        loginButton = findViewById(R.id.button_login);

        loginButton.setOnClickListener(this::onLoginClick);
    }

    private boolean checkValid() {
        if (usernameField.getText().toString().isBlank()) {
            textError.setText(R.string.error_username_empty);
            return false;
        }

        if (passwordField.getText().toString().isBlank()) {
            textError.setText(R.string.error_password_empty);
            return false;
        }

        return true;
    }

    private void onLoginClick(View view) {
        textError.setText("");
        if (!checkValid()) {
            return;
        }

        loginButton.setEnabled(false);

        try {
            final var body = new JSONObject();
            body.put("username", usernameField.getText());
            body.put("password", passwordField.getText());

            final var req = new Request.Builder()
                    .url(Constants.BACKEND_URL + "login")
                    .post(RequestBody.create(body.toString(), Constants.JSON))
                    .build();

            Constants.HTTP_CLIENT.newCall(req).enqueue(new PostLoginCallback());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchUserInfo(String token) {
        Log.d("User Info", "Fetching user info..."); // ✅ Kiểm tra xem có gọi không
        String username = usernameField.getText().toString().trim(); // Lấy username từ input

        Request request = new Request.Builder()
                .url(Constants.BACKEND_URL + "profiles?username=" + username) // Thêm username vào URL
                .addHeader("Authorization", "Bearer " + token)
                .build();

        Constants.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("User Info", "Request failed: " + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    Log.d("User Info", "Response received, code: " + response.code());
                });

                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject userObj = new JSONObject(responseBody);
                        runOnUiThread(() -> Log.d("User Info", "Fetched: " + userObj));

                        // Lưu thông tin user
                        Authentication.saveUserData(LoginActivity.this, userObj);

                    } catch (Exception e) {
                        Log.e("User Info", "JSON Parse Error: " + e.getMessage());
                    }
                } else {
                    Log.e("User Info", "Failed response: " + response.code());
                }
            }
        });
    }

    private void fetchUserInfo(String token, OnUserIdFetchedListener listener) {
        Log.d("User Info", "Fetching user info...");

        String username = usernameField.getText().toString().trim();
        Request request = new Request.Builder()
                .url(Constants.BACKEND_URL + "profiles?username=" + username)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        Constants.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("User Info", "Request failed: " + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject userObj = new JSONObject(responseBody);
                        String userId = userObj.getString("id");

                        runOnUiThread(() -> {
                            Log.d("User Info", "Fetched userId: " + userId);
                            listener.onUserIdFetched(userId);
                        });

                        saveUserToFirebase(userObj);

                    } catch (Exception e) {
                        Log.e("User Info", "JSON Parse Error: " + e.getMessage());
                    }
                } else {
                    Log.e("User Info", "Failed response: " + response.code());
                }
            }
        });
    }

    private void saveUserToFirebase(JSONObject userObj) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        try {
            String userId = userObj.getString("id"); // ID người dùng
            String username = userObj.getString("username");
            String email = userObj.getString("email");
            String avatar = userObj.optString("avatar", ""); // Nếu không có thì để trống

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("username", username);
            userMap.put("email", email);
            userMap.put("userId", userId);
            userMap.put("avatar", avatar);
            userMap.put("updatedAt", System.currentTimeMillis());

            db.collection("users").document(userId)
                    .set(userMap, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "User data updated"))
                    .addOnFailureListener(e -> Log.e("Firebase", "Error updating user", e));

        } catch (Exception e) {
            Log.e("Firebase", "JSON Error: " + e.getMessage());
        }
    }

    interface OnUserIdFetchedListener {
        void onUserIdFetched(String userId);
    }

    private class PostLoginCallback implements Callback {

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                textError.setText(R.string.error_unknown);
                loginButton.setEnabled(true);
            });
        }

        private void handle(int code, String body) throws Exception {
            switch (code) {
                case 404:
                    textError.setText(R.string.error_username_not_found);
                    break;
                case 403:
                    textError.setText(R.string.error_password_incorrect);
                    break;
                case 200:
                    final var obj = new JSONObject(body);
                    String token = obj.getString("token");
                    Authentication.saveToken(LoginActivity.this, token);
                    Log.d("User Info", "Using token: " + token);

                    // Gọi API lấy userId trước khi cập nhật Firestore
                    FirebaseAuth.getInstance().signOut();

                    fetchUserInfo(token, userId -> {
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        auth.signInAnonymously()
                                .addOnSuccessListener(authResult -> {
                                    String firebaseUid = auth.getUid();

                                    // Cập nhật Firebase UID vào Firestore với userId thực sự
                                    db.collection("users").document(userId)
                                            .update("firebaseUid", firebaseUid)
                                            .addOnSuccessListener(aVoid -> Log.d("Firebase", "Firebase UID updated"))
                                            .addOnFailureListener(e -> Log.e("Firebase", "Error updating UID", e));
                                })
                                .addOnFailureListener(e -> Log.e("Firebase", "Anonymous sign-in failed", e));

                        final var intent = new Intent(LoginActivity.this, CameraActivity.class);
                        startActivity(intent);
                    });
                    break;
            }
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            final var body = response.body().string();
            runOnUiThread(() -> {
                loginButton.setEnabled(true);
                try {
                    handle(response.code(), body);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

    }

}
