package dev.frilly.locket.dto.req;

import dev.frilly.locket.Constants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * DTO for a request concerning all user's fields.
 */
public final class ProfileRequest {

  @Pattern(regexp = Constants.USERNAME_REGEX)
  private final String username = null;

  @Email
  private final String    email     = null;
  private final String    password  = null;
  private final LocalDate birthdate = null;


  public String username() {
    return username;
  }

  public String email() {
    return email;
  }

  public String password() {
    return password;
  }

  public LocalDate birthdate() {
    return birthdate;
  }

}
