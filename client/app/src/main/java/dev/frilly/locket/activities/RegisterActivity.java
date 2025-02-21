package dev.frilly.locket.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

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
 * The activity for handling registration.
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText usernameField;
    private TextInputEditText emailField;
    private TextInputEditText passwordField;
    private TextInputEditText retypeField;

    private TextView errorText;
    private MaterialButton continueButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_outer), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameField = findViewById(R.id.field_username);
        emailField = findViewById(R.id.field_email);
        passwordField = findViewById(R.id.field_password);
        retypeField = findViewById(R.id.field_confirm);
        errorText = findViewById(R.id.text_error);
        continueButton = findViewById(R.id.button_continue);

        continueButton.setOnClickListener(this::onContinue);
    }

    private void setEnabled(final boolean val) {
        usernameField.setEnabled(val);
        emailField.setEnabled(val);
        passwordField.setEnabled(val);
        retypeField.setEnabled(val);
        continueButton.setEnabled(val);
    }

    private boolean isInvalid() {
        if (usernameField.getText().toString().isBlank()) {
            errorText.setText(R.string.error_username_empty);
            return true;
        }

        if (emailField.getText().toString().isBlank()) {
            errorText.setText(R.string.error_email_empty);
            return true;
        }

        if (passwordField.getText().toString().isBlank()) {
            errorText.setText(R.string.error_password_empty);
            return true;
        }

        if (!retypeField.getText().toString().equals(passwordField.getText().toString())) {
            errorText.setText(R.string.error_password_different);
            return true;
        }

        return false;
    }

    private void onContinue(View view) {
        if (isInvalid()) {
            return;
        }

        setEnabled(false);
        try {
            final var body = new JSONObject();
            body.put("username", usernameField.getText());
            body.put("password", passwordField.getText());
            body.put("email", emailField.getText());

            final var req = new Request.Builder()
                    .url(Constants.BACKEND_URL + "register")
                    .post(RequestBody.create(body.toString(), Constants.JSON))
                    .build();

            Constants.HTTP_CLIENT.newCall(req).enqueue(new PostRegisterCallback());
        } catch (Exception e) {
            setEnabled(true);
            e.printStackTrace();
            errorText.setText(R.string.error_unknown);
        }
    }

    private void nextStep(String body) {
        try {
            final var obj = new JSONObject(body);
            Authentication.saveToken(this, obj.getString("token"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        final Intent intent = new Intent(this, Register2Activity.class);
        startActivity(intent);
    }

    private class PostRegisterCallback implements Callback {

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            e.printStackTrace();
            setEnabled(true);
            runOnUiThread(() -> errorText.setText(R.string.error_unknown));
        }

        private void parseBadRequestError(String body) {
            try {
                final var obj = new JSONObject(body);
                errorText.setText(obj.getString("message"));
            } catch (Exception e) {
                e.printStackTrace();
                errorText.setText(R.string.error_unknown);
            }
        }


        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            final String body = (response.body() != null) ? response.body().string() : null;
            response.close();

            runOnUiThread(() -> {
                setEnabled(true);
                switch (response.code()) {
                    case 409:
                        errorText.setText(R.string.error_username_taken);
                        break;
                    case 400:
                        parseBadRequestError(body);
                        break;
                    case 200:
                        nextStep(body);
                        break;
                }
            });
        }

    }

}
