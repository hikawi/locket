package dev.frilly.locket.controller.dto

/**
 * The response to send if there's an error in the route.
 */
data class ErrorResponse(
    val code: Int,
    val message: String,
)
