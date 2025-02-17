package dev.frilly.locket.component

import dev.frilly.locket.controller.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import java.time.format.DateTimeParseException

/**
 * The advice to catch bad requests and returns with a proper error message
 * to the requester.
 */
@ControllerAdvice
class GlobalBadRequestAdvice {

    /**
     * Handles the request when the `@Valid` annotations throw errors, for
     * example `@Email`.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(ex: MethodArgumentNotValidException): ErrorResponse {
        val firstErrorMessage =
            ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
                ?: "Invalid request"
        return ErrorResponse(400, firstErrorMessage)
    }

    /**
     * Handles when the JSON can not deserialize properly into a RequestBody.
     *
     * This usually happens when type mismatches happen that can't be parsed.
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleNotReadable(ex: HttpMessageNotReadableException): ErrorResponse {
        if (ex.rootCause is DateTimeParseException) {
            return ErrorResponse(
                400, "Fields of type Date or Time have incorrect formats"
            )
        }

        return ErrorResponse(400, "Missing fields")
    }

}