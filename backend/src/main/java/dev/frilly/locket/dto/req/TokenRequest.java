package dev.frilly.locket.dto.req;

import jakarta.validation.constraints.NotEmpty;

public record TokenRequest(@NotEmpty String token) {

}
