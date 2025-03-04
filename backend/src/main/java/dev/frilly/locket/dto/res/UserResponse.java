package dev.frilly.locket.dto.res;

import java.time.LocalDate;

/**
 * A response containing a user. Basically the same as a
 * {@link dev.frilly.locket.data.User} but without the password field.
 */
public record UserResponse(long id, String username, String email,
                           String avatar, LocalDate birthdate) {

}
