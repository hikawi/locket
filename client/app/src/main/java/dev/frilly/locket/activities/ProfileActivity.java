package dev.frilly.locket.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import dev.frilly.locket.R;

/**
 * The activity to view the user profile.
 */
public class ProfileActivity extends AppCompatActivity {

    private ImageButton buttonBack;

    private ImageView imageProfile;
    private ImageButton buttonChangeImage;
    private TextView textName;

    private Button buttonChangeEmail;
    private Button buttonAddWidget;
    private Button buttonLogout;
    private Button buttonDeleteAccount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_screen);

        buttonBack = findViewById(R.id.button_back);
        imageProfile = findViewById(R.id.image_profile);
        buttonChangeImage = findViewById(R.id.button_change_profile);
        textName = findViewById(R.id.text_name);
        buttonChangeEmail = findViewById(R.id.button_change_email);
        buttonAddWidget = findViewById(R.id.button_add_widget);
        buttonLogout = findViewById(R.id.button_logout);
        buttonDeleteAccount = findViewById(R.id.button_delete_account);

        buttonBack.setOnClickListener(this::onBackButton);
    }

    private void onBackButton(final View view) {
        getOnBackPressedDispatcher().onBackPressed();
    }

}
