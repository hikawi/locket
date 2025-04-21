package dev.frilly.locket.component;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * The component that exports several Google beans after configuring.
 */
@Component
public final class GoogleComponent {

  @Value("${secrets.oauth-web-token}")
  private String oauthWebClient;

  /**
   * Builds the Firebase Messaging instance for sending notifications.
   */
  @Bean
  public FirebaseMessaging messaging() {
    try {
      final var input = new FileInputStream("./locket-firebase-key.json");
      final var options = FirebaseOptions.builder()
          .setCredentials(GoogleCredentials.fromStream(input))
          .build();

      final var app = FirebaseApp.initializeApp(options);
      return FirebaseMessaging.getInstance();
    } catch (IOException ex) {
      throw new NullPointerException("Failed to initialize Firebase Messaging");
    }
  }

  /**
   * Builds the Google's OAuth Token verifier on the backend side.
   */
  @Bean
  public GoogleIdTokenVerifier googleIdTokenVerifier() {
    final var transport = new NetHttpTransport();
    final var factory   = GsonFactory.getDefaultInstance();

    return new GoogleIdTokenVerifier.Builder(transport, factory).setAudience(
        List.of(oauthWebClient)).build();
  }

}
