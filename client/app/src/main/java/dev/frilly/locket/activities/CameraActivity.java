package dev.frilly.locket.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.camera.core.Preview;
import androidx.camera.video.Recording;
import androidx.camera.video.Recorder;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.QualitySelector;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.frilly.locket.R;

public class CameraActivity extends AppCompatActivity {
    private boolean isFrontCamera = false;
    private boolean isRecording = false;
    private long pressStartTime;
    private static final int LONG_PRESS_DURATION = 500;

    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording currentRecording;
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;

    private PreviewView previewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_screen);

        previewView = findViewById(R.id.previewView);
        ImageButton captureButton = findViewById(R.id.captureButton);
        ImageButton switchCameraButton = findViewById(R.id.switchCameraButton);
        ImageButton galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setColorFilter(Color.WHITE);

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            startCamera();
        }

        switchCameraButton.setOnClickListener(v -> {
            isFrontCamera = !isFrontCamera;
            startCamera();
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
                        Toast.makeText(this, "Quyền bị từ chối!", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng app nếu không có quyền
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
                            (LifecycleOwner) this, cameraSelector, preview, imageCapture, videoCapture);
                } else {
                    Camera camera = cameraProvider.bindToLifecycle(
                            (LifecycleOwner) this, cameraSelector, preview, imageCapture);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Không thể khởi động camera!", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera chưa sẵn sàng!", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(CameraActivity.this, "Chụp ảnh thành công!", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        runOnUiThread(() -> Toast.makeText(CameraActivity.this, "Lỗi chụp ảnh: " + exception.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void startRecording() {
        if (videoCapture == null || isRecording) return;

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "video_" + System.currentTimeMillis());
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");

        MediaStoreOutputOptions options = new MediaStoreOutputOptions.Builder(
                getContentResolver(),
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build();

        currentRecording = videoCapture.getOutput()
                .prepareRecording(this, options)
                .start(ContextCompat.getMainExecutor(this), videoRecordEvent -> {
                    if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                        runOnUiThread(() -> Toast.makeText(CameraActivity.this, "Video đã lưu!", Toast.LENGTH_SHORT).show());
                    }
                });

        isRecording = true;
        Toast.makeText(this, "Bắt đầu quay video...", Toast.LENGTH_SHORT).show();
    }



    private void stopRecording() {
        if (currentRecording != null && isRecording) {
            currentRecording.stop();
            currentRecording = null;
            isRecording = false;
            Toast.makeText(this, "Dừng quay video!", Toast.LENGTH_SHORT).show();
        }
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
