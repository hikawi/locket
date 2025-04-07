package dev.frilly.locket.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import dev.frilly.locket.Authentication;
import dev.frilly.locket.Constants;
import dev.frilly.locket.R;
import dev.frilly.locket.adapter.ConfirmPostAdapter;
import dev.frilly.locket.adapter.SendToFriendsAdapter;
import dev.frilly.locket.room.entities.UserProfile;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

public class ConfirmPostActivity extends AppCompatActivity {
    private Context context;
    private ConfirmPostAdapter adapter;
    private LinearLayout dotsLayout;
    private ImageView[] dots;
    private String fileType;
    private String filePath;
    private ViewPager2 viewPager;
    private List<String> images;
    private String userLocation = "Unknown Location"; // Default
    private FusedLocationProviderClient fusedLocationClient;
    private SendToFriendsAdapter sendToFriendsAdapter;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_post_screen);
        context = getApplicationContext();

        ImageButton sendButton = findViewById(R.id.send_button);
        ImageButton cancelButton = findViewById(R.id.cancel_button);
        ImageButton downloadButton = findViewById(R.id.download_button);

        viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dots_layout);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Intent intent = getIntent();
        if (intent != null) {
            filePath = intent.getStringExtra("file_path");
            fileType = intent.getStringExtra("file_type");
            images = Collections.singletonList(filePath);
        }

        adapter = new ConfirmPostAdapter(this, images, userLocation);
        viewPager.setAdapter(adapter);

        // Show dots indicating screen position
        addDotsIndicator(); // Add dots dynamically
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDotsIndicator(position);
            }
        });

        getUserLocation();

        sendButton.setOnClickListener(v -> {
            runOnUiThread(() -> Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show());
            uploadFileToBackend();
        });

        cancelButton.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        downloadButton.setOnClickListener(v -> saveToGallery());

        // Show the list of friends to send
        RecyclerView friendsRecycler = findViewById(R.id.friends_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        friendsRecycler.setLayoutManager(layoutManager);

        Constants.ROOM.userProfileDao().getProfiles().observe(this, userProfiles -> {
            List<UserProfile> friends = userProfiles.stream()
                    .filter(p -> p.friendState == UserProfile.FriendState.FRIEND)
                    .collect(Collectors.toList());

            sendToFriendsAdapter = new SendToFriendsAdapter(this, friends);
            friendsRecycler.setAdapter(sendToFriendsAdapter);
        });

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void addDotsIndicator() {
        int NUMBER_OF_PAGES = 3;
        int dotsCount = images.size() * NUMBER_OF_PAGES;
        dots = new ImageView[dotsCount];

        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(getDrawable(R.drawable.dot_inactive)); // Default dark dot

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(20, 0, 20, 0);
            dotsLayout.addView(dots[i], params);
        }

        dots[0].setImageDrawable(getDrawable(R.drawable.dot_active)); // Highlight first dot as default
    }

    // Update active dot (indicating current page position)
    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateDotsIndicator(int position) {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setImageDrawable(getDrawable(R.drawable.dot_inactive));
        }
        dots[position].setImageDrawable(getDrawable(R.drawable.dot_active));
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                // Get address from location
                getAddressFromLocation(location);
            } else {
                Log.d("Location", "Last location is null, requesting new location...");

                // Request new location
                LocationRequest locationRequest = new LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY, 5000)
                        .setMinUpdateIntervalMillis(3000)
                        .build();

                fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                            Location location = locationResult.getLastLocation();
                            getAddressFromLocation(location);
                            fusedLocationClient.removeLocationUpdates(this);
                        }
                    }
                }, Looper.getMainLooper());
            }
        }).addOnFailureListener(e -> {
            Log.e("Location", "Failed to get location: " + e.getMessage());
            userLocation = "Failed to get location";
            adapter.updateLocation(userLocation);
        });
    }

    private void getAddressFromLocation(Location location) {
        if (location == null) {
            userLocation = "Location unavailable";
            adapter.updateLocation(userLocation);
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // Get location details
                String district = address.getSubLocality(); // District
                String city = address.getLocality(); // City
                String adminArea = address.getAdminArea(); // Sometimes city appears here
                String subAdminArea = address.getSubAdminArea(); // Alternative district
                String country = address.getCountryName(); // Country

                // Ensure district, city, country are not empty
                if (district == null || district.isEmpty()) {
                    district = (subAdminArea != null) ? subAdminArea : "? ";
                }
                if (city == null || city.isEmpty()) {
                    city = (adminArea != null) ? adminArea : "?";
                }
                if (country == null || country.isEmpty()) {
                    country = "?";
                }

                userLocation = district + ", " + city + ", " + country;
                Log.d("Location", "Retrieved location: " + userLocation);
            } else {
                userLocation = "Address not found";
                Log.d("Location", "Geocoder returned empty address list.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            userLocation = "Geocoder error";
            Log.e("Location", "Geocoder failed: " + e.getMessage());
        }

        adapter.updateLocation(userLocation);
    }

    private void uploadFileToBackend() {
        if (filePath == null || filePath.isEmpty()) {
            AlertDialog.Builder filePathEmptyBuilder = new AlertDialog.Builder(this);
            filePathEmptyBuilder.setTitle("Error Posting")
                    .setMessage("File path is empty! Please try again later")
                    .setNegativeButton("OK", null)
                    .show();
            return;
        }

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                AlertDialog.Builder noExistFileBuilder = new AlertDialog.Builder(this);
                noExistFileBuilder.setTitle("Error Posting")
                        .setMessage("File does not exist! Please try again later")
                        .setNegativeButton("OK", null)
                        .show();
                return;
            }

            // Detect MIME type dynamically
            String mimeType = getMimeType(filePath);
            if (mimeType == null || !mimeType.startsWith("image/")) {
                AlertDialog.Builder invalidImageBuilder = new AlertDialog.Builder(this);
                invalidImageBuilder.setTitle("Error Posting")
                        .setMessage("Invalid image file! Please try again later")
                        .setNegativeButton("OK", null)
                        .show();
                return;
            }

            Log.d("File Upload", "Uploading file: " + filePath);
            Log.d("File Debug", "Detected MIME Type: " + mimeType);

            // Get message text or time or location
            int currentItem = viewPager.getCurrentItem();
            int pageType = currentItem % 3; // 0 = Message, 1 = Time, 2 = Location
            String dataToSend = adapter.getMessageItem(pageType);

            // Get list of friends' id to see the post
            List<Long> selectedIds = sendToFriendsAdapter.getSelectedFriendIds();
            String viewers = selectedIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            // Build request body
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", file.getName(), RequestBody.create(file, MediaType.parse(mimeType)))
                    .addFormDataPart("message", dataToSend) // Add user message
                    .addFormDataPart("viewers", viewers)  // Add appropriate values if needed
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
            AlertDialog.Builder unknownPostErrorBuilder = new AlertDialog.Builder(this);
            unknownPostErrorBuilder.setTitle("Unknown Error Posting")
                    .setMessage("Fail to upload post! Please try again later")
                    .setNegativeButton("OK", null)
                    .show();
        }
    }

    private String getMimeType(String filePath) {
        Uri fileUri = Uri.fromFile(new File(filePath));
        String mimeType = getContentResolver().getType(fileUri);

        if (mimeType == null) {
            // Check file extension
            String extension = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
            switch (extension) {
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
                    AlertDialog.Builder builder413 = new AlertDialog.Builder(ConfirmPostActivity.this);
                    builder413.setTitle("Error Code 413")
                            .setMessage("Image is too big. Please try other image")
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getOnBackPressedDispatcher().onBackPressed();
                                }
                            })
                            .show();
                    break;
                case 401:
                    AlertDialog.Builder builder401 = new AlertDialog.Builder(ConfirmPostActivity.this);
                    builder401.setTitle("Error Code 401")
                            .setMessage("Authentication failed! Please log in again")
                            .setNegativeButton("OK", null)
                            .show();
                    break;
                case 400:
                    AlertDialog.Builder builder400 = new AlertDialog.Builder(ConfirmPostActivity.this);
                    builder400.setTitle("Error Code 400")
                            .setMessage("Invalid image. Please try other image")
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getOnBackPressedDispatcher().onBackPressed();
                                }
                            })
                            .show();
                    break;
                case 200:
                    AlertDialog.Builder builder200 = new AlertDialog.Builder(ConfirmPostActivity.this);
                    builder200.setTitle("Complete")
                            .setMessage("Image uploaded successfully!")
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getOnBackPressedDispatcher().onBackPressed();
                                }
                            })
                            .show();
                    break;
                default:
                    AlertDialog.Builder builderUnknown = new AlertDialog.Builder(ConfirmPostActivity.this);
                    builderUnknown.setTitle("Unknown Error")
                            .setMessage("Unknown error code. Please try again later")
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getOnBackPressedDispatcher().onBackPressed();
                                }
                            })
                            .show();
                    break;
            }
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            runOnUiThread(() -> {
                try {
                    handle(response.code());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void saveToGallery() {
        if (filePath == null || filePath.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy tệp!", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(this, "Tệp không tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();

        Uri collection;
        if ("image".equals(fileType)) {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
            values.put(MediaStore.Images.Media.MIME_TYPE, getMimeType(filePath));
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp");
        } else if ("video".equals(fileType)) {
            collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            values.put(MediaStore.Video.Media.DISPLAY_NAME, file.getName());
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            values.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/MyApp");
        } else {
            Toast.makeText(this, "Định dạng không được hỗ trợ!", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = resolver.insert(collection, values);
        if (uri == null) {
            Toast.makeText(this, "Không thể lưu tệp!", Toast.LENGTH_SHORT).show();
            return;
        }

        try (OutputStream outputStream = resolver.openOutputStream(uri);
             InputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            Toast.makeText(this, "Đã lưu vào bộ sưu tập!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lưu thất bại!", Toast.LENGTH_SHORT).show();
        }
    }
}