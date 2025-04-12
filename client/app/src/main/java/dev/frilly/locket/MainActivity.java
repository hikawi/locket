package dev.frilly.locket;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import dev.frilly.locket.activities.CameraActivity;
import dev.frilly.locket.activities.WelcomeActivity;
import dev.frilly.locket.room.LocalDatabase;
import dev.frilly.locket.utils.FirebaseUtil;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseFirestore.getInstance().clearPersistence();

        //Authentication.unauthenticate(this);
        Constants.ROOM = Room.databaseBuilder(getApplicationContext(), LocalDatabase.class,
                "locket-local").build();

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

    void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                Log.d("Token", "My token : " + token);

                FirebaseUtil.currentUserDetails().addOnSuccessListener(userRef -> {
                    if (userRef != null) {
                        userRef.update("fcmToken", token)
                                .addOnSuccessListener(aVoid -> Log.d("Token", "FCM Token updated successfully"))
                                .addOnFailureListener(e -> Log.e("Token", "Failed to update FCM Token", e));
                    } else {
                        Log.e("Token", "User reference is null");
                    }
                }).addOnFailureListener(e -> Log.e("Token", "Failed to get user reference", e));
            }
        });
    }

}