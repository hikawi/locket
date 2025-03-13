package dev.frilly.locket.firebase;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.io.IOException;

import dev.frilly.locket.Authentication;
import dev.frilly.locket.Constants;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        try {
            final var body = new JSONObject();
            body.put("token", token);

            // We need an authorization token to subscribe to firebase messaging service
            // but how do we know onNewToken() gets called after logging in.
            final var req = new Request.Builder()
                    .url(Constants.BACKEND_URL + "subscribe")
                    .header("Authorization", "Bearer " + Authentication.getToken(this))
                    .post(RequestBody.create(body.toString(), Constants.JSON))
                    .build();

            Constants.HTTP_CLIENT.newCall(req).enqueue(new TokenCallback());
        } catch (Exception ex) {

        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remotemessage) {
        super.onMessageReceived(remotemessage);
    }

    private static class TokenCallback implements Callback {

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            // Bro. What do I even put.
            // Help.
        }

    }

}
