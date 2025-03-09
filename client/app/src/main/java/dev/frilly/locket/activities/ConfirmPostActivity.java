package dev.frilly.locket.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

import dev.frilly.locket.Authentication;
import dev.frilly.locket.Constants;
import dev.frilly.locket.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConfirmPostActivity extends AppCompatActivity {
    private Context context;
    private ImageView imageView;
    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer;
    private ImageButton sendButton;
    private EditText messageInput;
    private String fileType;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_post_screen);
        context = getApplicationContext();

        imageView = findViewById(R.id.imageView);
        surfaceView = findViewById(R.id.surfaceView);
        sendButton = findViewById(R.id.send_button);
        messageInput = findViewById(R.id.message_confirm_input);

        Intent intent = getIntent();
        if (intent != null) {
            filePath = intent.getStringExtra("file_path");
            fileType = intent.getStringExtra("file_type");

            if (filePath != null) {
                if ("image".equals(fileType)) {
                    showImage(filePath);
                } else if ("video".equals(fileType)) {
                    showVideo(filePath);
                }
            }
        }

        sendButton.setOnClickListener(v -> uploadFileToBackend());
    }

    private void showImage(String path) {
        imageView.setVisibility(View.VISIBLE);
        surfaceView.setVisibility(View.GONE);
        imageView.setImageURI(Uri.fromFile(new File(path)));
    }

    private void showVideo(String path) {
        imageView.setVisibility(View.GONE);
        surfaceView.setVisibility(View.VISIBLE);

        mediaPlayer = new MediaPlayer();
        SurfaceHolder holder = surfaceView.getHolder();

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    mediaPlayer.setDisplay(holder);
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.setOnPreparedListener(mp -> {
                        mp.start();
                    });
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            }
        });
    }

    private void uploadFileToBackend() {
        if (filePath == null || filePath.isEmpty()) {
            Toast.makeText(this, "File path is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Toast.makeText(this, "File does not exist!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Detect MIME type dynamically
            String mimeType = getMimeType(filePath);
            if (mimeType == null || !mimeType.startsWith("image/")) {
                Toast.makeText(this, "Invalid image file!", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("File Upload", "Uploading file: " + filePath);
            Log.d("File Debug", "Detected MIME Type: " + mimeType);

            // Build request body
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", file.getName(), RequestBody.create(file, MediaType.parse(mimeType)))
                    .addFormDataPart("viewers", "")  // Add appropriate values if needed
                    .addFormDataPart("message", messageInput.getText().toString()) // Add user message
                    .build();

            // Create request
            Request request = new Request.Builder()
                    .url(Constants.BACKEND_URL + "posts")
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + Authentication.getToken(this))
                    .build();

            // Execute request asynchronously
            Constants.HTTP_CLIENT.newCall(request).enqueue(new MakePostCallBack());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to upload file!", Toast.LENGTH_SHORT).show();
        }
    }

    private String getMimeType(String filePath) {
        Uri fileUri = Uri.fromFile(new File(filePath));
        String mimeType = getContentResolver().getType(fileUri);

        if (mimeType == null) {
            // If Android cannot determine MIME type, use extension-based lookup
            String extension = filePath.substring(filePath.lastIndexOf(".") + 1);
            switch (extension.toLowerCase()) {
                case "jpg":
                case "jpeg":
                    return "image/jpeg";
                case "png":
                    return "image/png";
                case "gif":
                    return "image/gif";
                case "webp":
                    return "image/webp";
                default:
                    return null; // Unsupported type
            }
        }
        return mimeType;
    }

    private class MakePostCallBack implements Callback {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            runOnUiThread(() -> {
                Toast.makeText(context, "Unknown error has occurred", Toast.LENGTH_SHORT).show();
            });
            e.printStackTrace();
        }

        private void handle(int code) throws Exception {
            switch (code) {
                case 413:
                    runOnUiThread(() -> {
                        Toast.makeText(context, "Image is too big. Please try again", Toast.LENGTH_SHORT).show();
                    });
                    break;
                case 401:
                    runOnUiThread(() -> {
                        Toast.makeText(context, "Authentication failed! Please log in again", Toast.LENGTH_SHORT).show();
                    });
                    break;
                case 400:
                    runOnUiThread(() -> {
                        Toast.makeText(context, "Invalid image. Please try again", Toast.LENGTH_SHORT).show();
                    });
                    break;
                case 201:
                    runOnUiThread(() -> {
                        Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                    });
                    break;
                default:
                    runOnUiThread(() -> {
                        Toast.makeText(context, "Unknown error code", Toast.LENGTH_SHORT).show();
                    });
                    break;
            }
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            runOnUiThread(() -> {
                try {
                    Log.d("Code: ", "" + response.code());
                    handle(response.code());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
