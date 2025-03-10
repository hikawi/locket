package dev.frilly.locket;

import org.intellij.lang.annotations.RegExp;

/**
 * Static class of constants that is reused within the code.
 */
public final class Constants {

  /**
   * The regex for usernames.
   */
  @RegExp
  public static final String USERNAME_REGEX = "[a-zA-Z-_][a-zA-Z-_0-9]+";

  /**
   * The image link for the app icon, for Firebase Messaging.
   */
  public static final String APP_IMAGE
      = "https://res.cloudinary.com/dvt4rehn7/image/upload/locket";

  private Constants() {
    // Left blank
  }

}
