package dev.frilly.locket;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import dev.frilly.locket.activities.WelcomeActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: Remove this
        Authentication.unauthenticate(this);

        if (Authentication.isAuthenticated(this)) {
            // TODO: Go straight to main screen.
        } else {
            final var intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
        }
    }

}