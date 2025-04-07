package dev.frilly.locket.activities;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.content.ContentResolver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import dev.frilly.locket.Authentication;
import dev.frilly.locket.Constants;
import dev.frilly.locket.R;
import dev.frilly.locket.adapter.SendToFriendsAdapter;
import dev.frilly.locket.room.entities.UserProfile;
import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.MediaType;
import okhttp3.Callback;
import okhttp3.Response;

public class ShareBottomSheetDialog extends BottomSheetDialogFragment {
    private SendToFriendsAdapter sendToFriendsAdapter;

    private String imageUrl;

    public ShareBottomSheetDialog(String imageUrl) {
        this.imageUrl = imageUrl;
    }



    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setDimAmount(0.5f);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_share, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView friendsRecycler = view.findViewById(R.id.friends_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        friendsRecycler.setLayoutManager(layoutManager);

        Constants.ROOM.userProfileDao().getProfiles().observe(getViewLifecycleOwner(), new Observer<List<UserProfile>>() {
            @Override
            public void onChanged(List<UserProfile> userProfiles) {
                List<UserProfile> friends = userProfiles.stream()
                        .filter(p -> p.friendState == UserProfile.FriendState.FRIEND)
                        .collect(Collectors.toList());

                sendToFriendsAdapter = new SendToFriendsAdapter(getContext(), friends);
                friendsRecycler.setAdapter(sendToFriendsAdapter);
            }
        });

        View shareButton = view.findViewById(R.id.share_post_button);
        shareButton.setOnClickListener(v -> {
            List<Long> selectedIds = sendToFriendsAdapter.getSelectedFriendIds();

            if (selectedIds.isEmpty()) {
                Toast.makeText(getContext(), "Hãy chọn ít nhất một bạn bè", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadFileToBackend(selectedIds);
        });

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        return dialog;
    }

    private void uploadFileToBackend(List<Long> selectedIds) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(imageUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.connect();

                String mimeType = getMimeTypeFromUrl(imageUrl); // thêm hàm này bên dưới
                String extension = mimeType != null ? mimeType.split("/")[1] : "jpg"; // ví dụ: "jpeg", "png"
                File cacheFile = new File(requireContext().getCacheDir(), "shared_image." + extension);
                java.io.InputStream input = connection.getInputStream();
                java.io.FileOutputStream output = new java.io.FileOutputStream(cacheFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }

                output.close();
                input.close();

                // Tiếp tục upload
                requireActivity().runOnUiThread(() -> doUpload(cacheFile, selectedIds));

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void doUpload(File file, List<Long> selectedIds) {

        String mimeType = getMimeType(file.getAbsolutePath());
        Log.d("UPLOAD_DEBUG", "Uploading file: " + file.getAbsolutePath() + " | MIME: " + mimeType);

        if (mimeType == null || !mimeType.startsWith("image/")) {
            Toast.makeText(getContext(), "Tệp không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        String viewers = selectedIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", file.getName(),
                        RequestBody.create(file, MediaType.parse(mimeType)))
                .addFormDataPart("message", " ")
                .addFormDataPart("viewers", viewers)
                .build();

        Request request = new Request.Builder()
                .url(Constants.BACKEND_URL + "posts")
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + Authentication.getToken(getContext()))
                .build();

        Constants.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Lỗi khi đăng", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Đăng thành công!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    } else {
                        try {
                            String errorBody = response.body() != null ? response.body().string() : "null";
                            Log.e("UPLOAD_ERROR", "Response code: " + response.code() + " | Body: " + errorBody);
                            Toast.makeText(getContext(), "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Lỗi không rõ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }


    private String getMimeType(String filePath) {
        String extension = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "webp": return "image/webp";
            default: return null;
        }
    }

    private String getMimeTypeFromUrl(String url) {
        String extension = url.substring(url.lastIndexOf(".") + 1).toLowerCase();
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
                return null;
        }
    }

}
