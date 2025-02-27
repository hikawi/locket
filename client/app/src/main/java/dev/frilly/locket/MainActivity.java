package dev.frilly.locket;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import dev.frilly.locket.activities.CameraActivity;
import dev.frilly.locket.activities.WelcomeActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Authentication.unauthenticate(this);

        // If the user is already logged in, saved in SharedPreferences,
        // continue the main activity.
        // If not, get to the screen to either login/register.
        if (Authentication.isAuthenticated(this)) {
            final var intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        } else {
            final var intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
        }
    }

}