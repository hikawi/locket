package dev.frilly.locket.controller.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

/**
 * Schema class for a request body on POST /login.
 */
data class LoginPostRequest @JsonCreator constructor(
    @NotNull
    @Pattern(regexp = "[\\w-_][\\w\\d-_]+")
    @JsonProperty("username")
    val username: String,

    @NotNull
    @JsonProperty("password")
    val password: String,
)

/**
 * Schema class for a response body after /login.
 *
 * This may be reused for /register.
 */
data class LoginPostResponse(
    @NotNull
    val token: String,
)
