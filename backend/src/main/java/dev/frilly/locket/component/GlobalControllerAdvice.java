package dev.frilly.locket.component;

import dev.frilly.locket.dto.res.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeParseException;

/**
 * The component responsible for handling exceptions thrown within controller
 * routes.
 */
@RestControllerAdvice
public final class GlobalControllerAdvice {

  /**
   * Handles the request when the `@Valid` annotations throw errors, for
   * example `@Email`.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleValidation(final MethodArgumentNotValidException ex) {
    final var message = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .findFirst();
    return new ErrorResponse(400,
        message.map(FieldError::getDefaultMessage).orElse("Invalid request"));
  }

  /**
   * Handles when the JSON can not deserialize properly into a RequestBody.
   * <p>
   * This usually happens when type mismatches happen that can't be parsed.
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleNotReadable(final HttpMessageNotReadableException ex) {
    if (ex.getRootCause() instanceof DateTimeParseException) {
      return new ErrorResponse(400,
          "Fields of type Date or Time have incorrect formats");
    }

    return new ErrorResponse(400, "Can't understand HTTP message");
  }

  /**
   * Handles when a GET request's query parameters are not present.
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleMissingParameter(final MissingServletRequestParameterException ex) {
    return new ErrorResponse(400,
        "Missing param: %s".formatted(ex.getParameterName()));
  }

  /**
   * Handles when a multipart/form-data route doesn't receive all the
   * information it wants.
   */
  @ExceptionHandler(MissingServletRequestPartException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleMissingPart(final MissingServletRequestPartException ex) {
    return new ErrorResponse(400,
        "Missing part: %s".formatted(ex.getRequestPartName()));
  }

  /**
   * Handles when a {@link ResponseStatusException} is thrown.
   *
   * @param ex the exception
   *
   * @return the error response
   */
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleStatusException(final ResponseStatusException ex) {
    return ResponseEntity.status(ex.getStatusCode())
        .body(new ErrorResponse(ex.getStatusCode().value(),
            String.valueOf(ex.getReason())));
  }

  @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ErrorResponse handleMaxUploadSize(final MaxUploadSizeExceededException ex) {
    return new ErrorResponse(ex.getStatusCode().value(), ex.getMessage());
  }

}
