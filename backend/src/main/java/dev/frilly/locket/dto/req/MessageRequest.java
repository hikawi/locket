package dev.frilly.locket.dto.req;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

/**
 * Dto for a request containing messages data.
 */
public record MessageRequest(@Positive long receiver,
                             @NotEmpty String content) {

}
