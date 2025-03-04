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

  private Constants() {
    // Left blank
  }

}
