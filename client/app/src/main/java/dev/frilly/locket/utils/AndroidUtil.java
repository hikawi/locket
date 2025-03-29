package dev.frilly.locket.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import dev.frilly.locket.model.User;

public class AndroidUtil {

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Generates an intent and go to that activity.
     *
     * @param ctx the context
     * @param to  the class to move to
     */
    public static void moveScreen(final Context ctx, final Class<? extends Activity> to) {
        final var intent = new Intent(ctx, to);
        ctx.startActivity(intent);
    }

    /**
     * Apply insets to push in the activity.
     *
     * @param act   the activity
     * @param resId the resource to fit
     */
    public static void applyInsets(final Activity act, final int resId) {
        final var view = act.findViewById(resId);

        // Set Listener
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            final var systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Converts a dateString of form yyyy-MM-dd to a long representing the epoch milliseconds.
     *
     * @param dateString the date string
     * @return the epoch milliseconds
     */
    public static long dateStringToMillis(final String dateString) {
        final var formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        final var localDate = LocalDate.parse(dateString, formatter);
        return localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    public static void passUserModelAsIntent(Intent intent, User model) {
        intent.putExtra("username", model.getUsername());
        intent.putExtra("email", model.getEmail());
        intent.putExtra("userId", model.getUserId());
    }

    public static User getUserModelFromIntent(Intent intent) {
        User user = new User();
        user.setUsername(intent.getStringExtra("username"));
        user.setEmail(intent.getStringExtra("email"));
        user.setUserId(intent.getStringExtra("userId"));
        return user;
    }

    public static void setProfilePic(Context context, Uri imageUri, ImageView imageView) {
        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
    }

}
