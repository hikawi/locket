package dev.frilly.locket.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

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

}
