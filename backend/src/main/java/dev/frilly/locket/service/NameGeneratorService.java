package dev.frilly.locket.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import dev.frilly.locket.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;

/**
 * Represents the object that provides a service to generating names for
 * logged-in-by-Google accounts.
 */
@Service
public final class NameGeneratorService {

  @Autowired
  private GoogleIdTokenVerifier tokenVerifier;

  @Autowired
  private SecureRandom secureRandom;

  @Autowired
  private UserRepository userRepository;

  /**
   * Generates a unique username from the provided namesake.
   *
   * @param name The name to generate from
   *
   * @return A unique username generated
   */
  public String generateUsername(final String name) {
    // Attempt to slugify the name from the Google's profile to provide
    var slug = Normalizer.normalize(name, Form.NFC)
        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
        .replaceAll("\\s+", "-")
        .toLowerCase(Locale.ROOT);

    // If the slug starts with a number, we append a _ before it.
    if (slug.matches("^\\d")) {
      slug = "_" + slug;
    }

    while (true) {
      // Add random numbers behind.
      final var username = slug + secureRandom.nextInt(10000);

      // Check if the username is not taken.
      if (userRepository.findByUsername(username).isEmpty()) {
        return username;
      }
    }
  }

}
