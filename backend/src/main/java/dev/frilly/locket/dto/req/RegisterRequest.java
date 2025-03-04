package dev.frilly.locket.dto.req;

import dev.frilly.locket.Constants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for a request that needs all the credentials necessary to register a
 * new user.
 */
public final class RegisterRequest {

  @NotEmpty
  @Pattern(regexp = Constants.USERNAME_REGEX)
  private final String username;

  @Email
  @NotEmpty
  private final String email;

  @NotEmpty
  private final String password;

  public RegisterRequest(String username, String email, String password) {
    this.username = username;
    this.email    = email;
    this.password = password;
  }

  public String username() {
    return username;
  }

  public String email() {
    return email;
  }

  public String password() {
    return password;
  }

}
