package dev.frilly.locket.dto.req;

import dev.frilly.locket.Constants;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Schema for a request DTO that has login credentials.
 */
public record CredentialsRequest(
    @NotNull @Pattern(regexp = Constants.USERNAME_REGEX) String username,
    @NotNull String password) {

}
