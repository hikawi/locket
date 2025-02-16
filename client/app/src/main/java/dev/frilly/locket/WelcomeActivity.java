package dev.frilly.locket;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_outer), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
