package dev.frilly.locket.dto.res;

import java.time.LocalDateTime;

/**
 * Represents an abstract post.
 *
 * @param id        the post id
 * @param poster    the uploader
 * @param imageLink the image
 * @param message   the message
 * @param time      the time
 */
public record PostResponse(long id, UserResponse poster, String imageLink,
                           String message, LocalDateTime time) {

}
