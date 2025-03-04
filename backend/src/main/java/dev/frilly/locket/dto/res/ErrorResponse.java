package dev.frilly.locket.dto.res;

/**
 * An error message that contains a status code and a message for the error.
 */
public record ErrorResponse(int code, String message) {

}
