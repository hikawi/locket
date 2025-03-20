package dev.frilly.locket.dto.req;

import jakarta.validation.constraints.NotEmpty;

/**
 * DTO for a request needing to send a single parameter which is the username.
 *
 * @param username the username
 */
public record UsernameRequest(@NotEmpty String username) {

}
