package dev.frilly.locket;

/**
 * Represents a record for the backend's version. Includes a major, minor and
 * patch version.
 */
public record Versioning(int major, int minor, int patch) {

  private static final Versioning CURRENT = new Versioning(0, 5, 1);

  /**
   * Gets the programmatic version this backend is running on.
   *
   * @return the version instance
   */
  public static Versioning getCurrent() {
    return CURRENT;
  }

  @Override
  public String toString() {
    return "%d.%d.%d".formatted(major, minor, patch);
  }

}
