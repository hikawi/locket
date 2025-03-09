package dev.frilly.locket;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import dev.frilly.locket.activities.CameraActivity;
import dev.frilly.locket.activities.ChatActivity;
import dev.frilly.locket.activities.WelcomeActivity;
import dev.frilly.locket.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    private PreferenceManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Authentication.unauthenticate(this);

        // If the user is already logged in, saved in SharedPreferences,
        // continue the main activity.
        // If not, get to the screen to either login/register.
        if (Authentication.isAuthenticated(this)) {
            Log.d("AuthCheck", "User is authenticated");
            final var intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        } else {
            final var intent = new Intent(this, WelcomeActivity.class);
            Log.d("AuthCheck", "User is NOT authenticated");
            startActivity(intent);
        }
    }

}