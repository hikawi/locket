package dev.frilly.locket.dto.req;

import dev.frilly.locket.Constants;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Schema for a request DTO that has login credentials.
 */
public final class CredentialsRequest {

  @NotNull
  @Pattern(regexp = Constants.USERNAME_REGEX)
  private final String username;

  @NotNull
  private final String password;

  public CredentialsRequest(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public String username() {
    return username;
  }

  public String password() {
    return password;
  }

}
