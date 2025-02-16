package dev.frilly.locket.component

import dev.frilly.locket.controller.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * The advice to catch bad requests and returns with a proper error message
 * to the requester.
 */
@ControllerAdvice
class GlobalBadRequestAdvice {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(ex: MethodArgumentNotValidException): ErrorResponse {
        val firstErrorMessage =
            ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
                ?: "Invalid request"
        return ErrorResponse(400, firstErrorMessage)
    }

}