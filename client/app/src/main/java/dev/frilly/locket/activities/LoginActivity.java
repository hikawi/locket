package dev.frilly.locket.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

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
                    Authentication.saveToken(LoginActivity.this, obj.getString("token"));

                    final var intent = new Intent(LoginActivity.this, CameraActivity.class);
                    startActivity(intent);
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
