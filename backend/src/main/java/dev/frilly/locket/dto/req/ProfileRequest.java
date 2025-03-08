package dev.frilly.locket.dto.req;

import dev.frilly.locket.Constants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * DTO for a request concerning all user's fields.
 */
public record ProfileRequest(
    @Pattern(regexp = Constants.USERNAME_REGEX) String username,
    @Email String email, String password, LocalDate birthdate) {

}
