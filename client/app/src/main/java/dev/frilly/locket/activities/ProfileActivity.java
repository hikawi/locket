package dev.frilly.locket.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import dev.frilly.locket.Authentication;
import dev.frilly.locket.Constants;
import dev.frilly.locket.R;
import dev.frilly.locket.utils.AndroidUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

    private ActivityResultLauncher<Intent> mediaPickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_screen);

        AndroidUtil.applyInsets(this, R.id.layout_outer);

        buttonBack = findViewById(R.id.button_back);
        imageProfile = findViewById(R.id.image_profile);
        buttonChangeImage = findViewById(R.id.button_change_profile);
        textName = findViewById(R.id.text_name);
        buttonChangeEmail = findViewById(R.id.button_change_email);
        buttonAddWidget = findViewById(R.id.button_add_widget);
        buttonLogout = findViewById(R.id.button_logout);
        buttonDeleteAccount = findViewById(R.id.button_delete_account);

        buttonBack.setOnClickListener(v -> finish());
        buttonChangeEmail.setOnClickListener(v -> AndroidUtil.moveScreen(this,
                ChangeEmailActivity.class));
        buttonLogout.setOnClickListener(this::onLogoutButton);
        imageProfile.setOnClickListener(this::onChangeImageButton);
        buttonChangeImage.setOnClickListener(this::onChangeImageButton);
        buttonDeleteAccount.setOnClickListener(this::onDeleteAccountButton);

        mediaPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleMediaSelection(result.getData());
                    }
                }
        );


        loadInfo();
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        final var cursor = getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }

    private void handleMediaSelection(Intent data) {
        ArrayList<Uri> newUris = new ArrayList<>();

        if (data.getClipData() != null) {
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                Uri uri = data.getClipData().getItemAt(i).getUri();
                newUris.add(uri);
            }
        } else if (data.getData() != null) {
            Uri uri = data.getData();
            newUris.add(uri);
        }

        if (!newUris.isEmpty()) {
            Uri selectedMediaUri = newUris.get(0); // Get the first selected file

            // Convert URI to file path
            String filePath = getRealPathFromURI(selectedMediaUri);

            if (filePath != null) {
                // Determine the media type
                String fileType = getContentResolver().getType(selectedMediaUri);
                putProfileImage(filePath, fileType);
            } else {
                Toast.makeText(this, "Failed to get file path", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void putProfileImage(final String filePath, final String fileType) {
        final var file = new File(filePath);
        final var body = new MultipartBody.Builder()
                .addFormDataPart("image", filePath,
                        RequestBody.create(file, MediaType.parse(fileType)))
                .build();

        final var req = new Request.Builder()
                .header("Authorization", "Bearer " + Authentication.getToken(this))
                .url(Constants.BACKEND_URL + "profiles")
                .put(body)
                .build();

        Constants.HTTP_CLIENT.newCall(req).enqueue(new PutImageCallback());
    }

    private void loadInfo() {
        try {
            final var profile = Authentication.getUserData(this);
            Log.i("ProfileActivity", profile.toString());

            final var avatar = profile.getString("avatar");
            if (avatar != null)
                Glide.with(this).load(avatar).into(imageProfile);

            final var name = profile.getString("username");
            textName.setText(name);
        } catch (JSONException e) {
            Log.e("ProfileActivity", e.getMessage(), e);
        }
    }


    private void onChangeImageButton(final View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        mediaPickerLauncher.launch(Intent.createChooser(intent, "Select Media"));
    }

    private void onLogoutButton(final View view) {
        Authentication.unauthenticate(this);

        final var intent = new Intent(this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void onDeleteAccountButton(final View view) {
        final var req = new Request.Builder()
                .url(Constants.BACKEND_URL + "accounts")
                .header("Authorization", "Bearer " + Authentication.getToken(this))
                .delete()
                .build();

        Constants.HTTP_CLIENT.newCall(req).enqueue(new DeleteAccountCallback());
    }

    private class PutImageCallback implements Callback {

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            Log.e("PutImageCallback", e.getMessage(), e);
            runOnUiThread(() ->
                    AndroidUtil.showToast(ProfileActivity.this, getString(R.string.error_unknown)));
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            if (!response.isSuccessful()) {
                runOnUiThread(() ->
                        AndroidUtil.showToast(ProfileActivity.this, getString(R.string.error_unknown)));
                return;
            }

            try {
                final var body = new JSONObject(response.body().string());
                Authentication.saveUserData(ProfileActivity.this, body);
                runOnUiThread(ProfileActivity.this::loadInfo);
            } catch (JSONException e) {
                Log.e("PutImageCallback", e.getMessage(), e);
            }
        }

    }

    private class DeleteAccountCallback implements Callback {

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            Log.e("DeleteAccountCallback", e.getMessage(), e);
            runOnUiThread(() -> AndroidUtil.showToast(ProfileActivity.this,
                    getString(R.string.error_unknown)));
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            if (!response.isSuccessful()) {
                runOnUiThread(() -> AndroidUtil.showToast(ProfileActivity.this,
                        getString(R.string.error_unknown)));
                return;
            }

            Authentication.unauthenticate(ProfileActivity.this);
            Authentication.saveUserData(ProfileActivity.this, null);
        }

    }

}
