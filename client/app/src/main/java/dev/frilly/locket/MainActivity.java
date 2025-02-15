package dev.frilly.locket;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText usernameField;
    private EditText passwordField;
    private TextView errorText;

    private Button loginButton;
    private Button registerButton;

    private CompletableFuture<Integer> postJson(final String route, final String username, final String password) {
        try {
            final var url = new URL(Constants.BACKEND_URL + route);
            final var reqBody = new JSONObject();
            reqBody.put("username", username);
            reqBody.put("password", password);

            final var req = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(reqBody.toString(), Constants.JSON))
                    .build();

            final var future = new CompletableFuture<Integer>();
            Constants.HTTP_CLIENT.newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    future.complete(400);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    future.complete(response.code());
                }
            });
            return future;
        } catch (Exception ex) {
            ex.printStackTrace();
            return CompletableFuture.completedFuture(400);
        }
    }

    private void onClickLogin(final View view) {
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);

        errorText.setText("");
        if (usernameField.getText().toString().isBlank()) {
            errorText.setText(R.string.error_username_empty);
            return;
        }

        if (passwordField.getText().toString().isBlank()) {
            errorText.setText(R.string.error_password_empty);
            return;
        }

        final var future = postJson("login", usernameField.getText().toString(),
                passwordField.getText().toString());
        future.thenAccept(code -> runOnUiThread(() -> {
            switch (code) {
                case 404:
                    errorText.setText(R.string.error_username_not_found);
                    break;
                case 403:
                    errorText.setText(R.string.error_password_incorrect);
                    break;
                case 200:
                    Toast.makeText(this, "Logged in!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    errorText.setText(R.string.error_unknown);
                    break;
            }
        })).exceptionally((Throwable ex) -> {
            ex.printStackTrace();
            runOnUiThread(() -> errorText.setText(R.string.error_unknown));
            return null;
        }).thenRun(() -> runOnUiThread(() -> {
            loginButton.setEnabled(true);
            registerButton.setEnabled(true);
        }));
    }

    private void onClickRegister(final View view) {
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);

        errorText.setText("");
        if (usernameField.getText().toString().isBlank()) {
            errorText.setText(R.string.error_username_empty);
            return;
        }

        if (passwordField.getText().toString().isBlank()) {
            errorText.setText(R.string.error_password_empty);
            return;
        }

        final var future = postJson("register", usernameField.getText().toString(),
                passwordField.getText().toString());
        future.thenAccept(code -> runOnUiThread(() -> {
            switch (code) {
                case 409:
                    errorText.setText(R.string.error_username_taken);
                    break;
                case 200:
                    Toast.makeText(this, "Registered!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    errorText.setText(R.string.error_unknown);
                    break;
            }
        })).exceptionally((Throwable ex) -> {
            ex.printStackTrace();
            runOnUiThread(() -> errorText.setText(R.string.error_unknown));
            return null;
        }).thenRun(() -> runOnUiThread(() -> {
            loginButton.setEnabled(true);
            registerButton.setEnabled(true);
        }));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_form);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_outer), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameField = findViewById(R.id.field_username);
        passwordField = findViewById(R.id.field_password);
        errorText = findViewById(R.id.text_error);
        loginButton = findViewById(R.id.button_login);
        registerButton = findViewById(R.id.button_register);

        loginButton.setOnClickListener(this::onClickLogin);
        registerButton.setOnClickListener(this::onClickRegister);
    }

}