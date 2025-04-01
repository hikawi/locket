package dev.frilly.locket.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.frilly.locket.Constants;
import dev.frilly.locket.R;
import dev.frilly.locket.room.entities.UserProfile;
import dev.frilly.locket.services.FriendService;
import dev.frilly.locket.utils.AndroidUtil;

/**
 * Make a post with image
 */

public class CameraActivity extends AppCompatActivity {
    private static final int LONG_PRESS_DURATION = 500;
    private Context context;
    private boolean isFrontCamera = false;
    private boolean isRecording = false;
    private long pressStartTime;
    //private RecyclerView imageList;
    //private MediaAdapter mediaAdapter;
    private ImageButton userAvatar;
    private Button friendsButton;
    private ImageButton sendMessages;
    private ImageButton captureButton;
    private ImageButton chooseFromAlbum;
    private ImageButton switchCameraButton;
    private Button historyButton;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording currentRecording;
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;

    private PreviewView previewView;
    private ActivityResultLauncher<Intent> mediaPickerLauncher;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_screen);

        userAvatar = findViewById(R.id.main_user_avatar);
        friendsButton = findViewById(R.id.friends);
        sendMessages = findViewById(R.id.message);
        captureButton = findViewById(R.id.take_photo);
        chooseFromAlbum = findViewById(R.id.menu_button);
        switchCameraButton = findViewById(R.id.share_button);
        historyButton = findViewById(R.id.history_button);
        chooseFromAlbum.setColorFilter(Color.WHITE);

        previewView = findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            startCamera();
        }

        // Fetch friends and put in the friends button how many friends we fetched.
        FriendService.getInstance().fetchFriends(this).thenAccept(status -> {
            Log.d("CameraActivity", "Fetched friends accepted status " + status);
            if (!status)
                return;

            final var friends = Constants.ROOM.userProfileDao().getProfiles();
            runOnUiThread(() -> friends.observe(this, liveData -> {
                Log.d("CameraActivity", "Live data updated, total profiles: " + liveData.size());
                for (UserProfile profile : liveData) {
                    Log.d("CameraActivity", "User: " + profile.id +
                            ", Name: " + profile.username +
                            ", Friend State: " + profile.friendState);
                }
                final var count = liveData.stream().filter(d -> d.friendState == UserProfile.FriendState.FRIEND)
                        .count();
                friendsButton.setText(String.format("%d friend%s", count, count == 1 ? "" : "s"));
                friendsButton.invalidate();
            }));
        });

        // Initialize ActivityResultLauncher
        mediaPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleMediaSelection(result.getData());
                    }
                }
        );

        friendsButton.setOnClickListener(v -> {
            final var intent = new Intent(this, FriendsActivity.class);
            startActivity(intent);
        });

        captureButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressStartTime = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_UP:
                    long pressDuration = System.currentTimeMillis() - pressStartTime;
                    if (pressDuration < LONG_PRESS_DURATION) {
                        takePhoto();
                    } else {
                        stopRecording();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!isRecording && System.currentTimeMillis() - pressStartTime >= LONG_PRESS_DURATION) {
                        startRecording();
                    }
                    break;
            }
            return true;
        });

        chooseFromAlbum.setOnClickListener(v -> pickMediaFiles());

        sendMessages.setOnClickListener(v -> {
            final var intent = new Intent(CameraActivity.this, RecentChatsActivity.class);
            startActivity(intent);
        });

        switchCameraButton.setOnClickListener(v -> {
            isFrontCamera = !isFrontCamera;
            startCamera();
        });

        historyButton.setOnClickListener(v -> {
            final var intent = new Intent(CameraActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        userAvatar.setOnClickListener(v -> AndroidUtil.moveScreen(this, ProfileActivity.class));
    }

    private void pickMediaFiles() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        mediaPickerLauncher.launch(Intent.createChooser(intent, "Select Media"));
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
                String mediaType = "unknown";

                if (fileType != null) {
                    if (fileType.startsWith("image/")) {
                        mediaType = "image";
                    } else if (fileType.startsWith("video/")) {
                        mediaType = "video";
                    }
                }

                openConfirmPostActivity(filePath, mediaType);
            } else {
                Toast.makeText(this, "Failed to get file path", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        startCamera();
                    } else {
                        Toast.makeText(this, "Access Denied!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }


    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                if (!cameraProviderFuture.isDone()) return;
                cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(isFrontCamera ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK)
                        .build();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                        .build();

                videoCapture = VideoCapture.withOutput(recorder);


                if (videoCapture != null) {
                    Camera camera = cameraProvider.bindToLifecycle(
                            this, cameraSelector, preview, imageCapture, videoCapture);
                } else {
                    Camera camera = cameraProvider.bindToLifecycle(
                            this, cameraSelector, preview, imageCapture);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Cannot use camera!", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera is not ready!", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = new File(getExternalFilesDir(null), "photo.jpg");
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputFileOptions, Executors.newSingleThreadExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        runOnUiThread(() -> {
                            Toast.makeText(CameraActivity.this, "Capture successfully!", Toast.LENGTH_SHORT).show();
                            openConfirmPostActivity(photoFile.getAbsolutePath(), "image");
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        runOnUiThread(() -> Toast.makeText(CameraActivity.this, "Error capturing: " + exception.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void startRecording() {
        if (videoCapture == null || isRecording) return;

        try {
            File videoFile = new File(getExternalFilesDir(null), "video_" + System.currentTimeMillis() + ".mp4");
            FileOutputOptions outputOptions = new FileOutputOptions.Builder(videoFile).build();

            currentRecording = videoCapture.getOutput()
                    .prepareRecording(this, outputOptions)
                    .start(ContextCompat.getMainExecutor(this), videoRecordEvent -> {
                        if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                            isRecording = true;
                            Toast.makeText(this, "Start recording...", Toast.LENGTH_SHORT).show();
                        } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                            isRecording = false;
                            VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) videoRecordEvent;
                            if (finalizeEvent.hasError()) {
                                Toast.makeText(this, "Error recording: " + finalizeEvent.getError(), Toast.LENGTH_SHORT).show();
                            } else {
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Video saved!", Toast.LENGTH_SHORT).show();
                                    openConfirmPostActivity(videoFile.getAbsolutePath(), "video");
                                });
                            }
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot make temporary file!", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (currentRecording != null && isRecording) {
            currentRecording.stop();
            currentRecording = null;
            isRecording = false;
            Toast.makeText(this, "Stop recording!", Toast.LENGTH_SHORT).show();
        }
    }

    private void openConfirmPostActivity(String filePath, String fileType) {
        Intent intent = new Intent(CameraActivity.this, ConfirmPostActivity.class);
        intent.putExtra("file_path", filePath);
        intent.putExtra("file_type", fileType);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}
