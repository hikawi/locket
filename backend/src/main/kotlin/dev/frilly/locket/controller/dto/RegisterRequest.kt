package dev.frilly.locket.controller.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern

/**
 * The request body schema for POST /register.
 */
data class RegisterPostRequest @JsonCreator constructor(
    @NotEmpty(message = "Username must be non-empty")
    @Pattern(
        regexp = "[\\w-_][\\w-_\\d]+",
        message = "Username must only contain letters and numbers.",
    )
    @JsonProperty("username")
    val username: String,

    @NotEmpty(message = "Email must not be empty")
    @field:Email(
        message = "Invalid email format",
        regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
    )
    @JsonProperty("email")
    val email: String,

    @NotEmpty(message = "Password can't be empty")
    @JsonProperty("password")
    val password: String,
)

/**
 * The body sent back as a response to POST /register.
 */
data class RegisterPostResponse(
    val token: String
)
