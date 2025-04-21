package dev.frilly.locket.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import dev.frilly.locket.R;
import dev.frilly.locket.utils.AndroidUtil;

public class RecentChatsActivity extends BaseActivity {

    private static final String TAG = "RecentChatsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_chats);
        AndroidUtil.applyInsets(this, R.id.layout_outer);
        Log.d(TAG, "onCreate: Activity started");

        // Thêm ChatFragment vào layout
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, new ChatFragment());
        fragmentTransaction.commit();
        Log.d(TAG, "onCreate: ChatFragment added");

        // Xử lý sự kiện khi bấm vào nút back
        ImageView backBtn = findViewById(R.id.back_btn);
        if (backBtn != null) {
            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Back button clicked");
                    finish(); // Đóng Activity hiện tại
                }
            });
        } else {
            Log.e(TAG, "Back button not found");
        }

        Button btnOpenMessenger = findViewById(R.id.btn_open_messenger);
        btnOpenMessenger.setOnClickListener(v -> {
            Intent intent = new Intent(this, MessengerActivity.class);
            startActivity(intent);
        });

        // Xử lý khi bấm nút back trên điện thoại
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "onBackPressed triggered");
                finish();
            }
        });
    }
}
