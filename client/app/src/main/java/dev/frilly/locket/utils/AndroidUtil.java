package dev.frilly.locket.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

}
