package dev.frilly.locket.dto.req;

import dev.frilly.locket.Constants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for a request that needs all the credentials necessary to register a
 * new user.
 */
public record RegisterRequest(
    @NotEmpty @Pattern(regexp = Constants.USERNAME_REGEX) String username,
    @Email @NotEmpty String email, @NotEmpty String password) {

}
