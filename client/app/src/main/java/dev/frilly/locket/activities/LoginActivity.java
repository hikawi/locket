package dev.frilly.locket;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * The activity for the login screen.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText usernameField;
    private EditText passwordField;

    private Button loginButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        usernameField = findViewById(R.id.field_username);
        passwordField = findViewById(R.id.field_password);
        loginButton = findViewById(R.id.button_login);
    }

}
