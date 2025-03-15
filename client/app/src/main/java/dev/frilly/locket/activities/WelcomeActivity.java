package dev.frilly.locket.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import dev.frilly.locket.R;
import dev.frilly.locket.utils.AndroidUtil;

/**
 * The activity to display the welcome screen with two login and register buttons.
 */
public class WelcomeActivity extends AppCompatActivity {

    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen);

        loginButton = findViewById(R.id.button_login);
        registerButton = findViewById(R.id.button_register);

        AndroidUtil.applyInsets(this, R.id.layout_outer);

        loginButton.setOnClickListener(e -> {
            final var intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        registerButton.setOnClickListener(e -> {
            final var intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }

}
