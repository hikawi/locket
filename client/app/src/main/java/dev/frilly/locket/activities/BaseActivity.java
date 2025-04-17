package dev.frilly.locket.activities;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyAppTheme();
    }

    protected void applyAppTheme() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkTheme;

        if (prefs.contains("dark_theme")) {
            isDarkTheme = prefs.getBoolean("dark_theme", false);
        } else {
            // Mặc định là tối nếu chưa từng được lưu
            isDarkTheme = true;
        }

        AppCompatDelegate.setDefaultNightMode(
                isDarkTheme ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

}



