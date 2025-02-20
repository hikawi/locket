package dev.frilly.locket.controller.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Pattern
import java.time.LocalDate

/**
 * DTO for PUT /profiles.
 *
 * If the field is null, the field is unchanged.
 */
data class PutProfilesRequest @JsonCreator constructor(
    @JsonProperty("name")
    @Pattern(regexp = "[\\w-_][\\d\\w-_]+")
    val username: String? = null,

    @JsonProperty("email")
    @Email
    val email: String? = null,

    @JsonProperty("password")
    val password: String? = null,

    @JsonProperty("birthdate")
    @Past
    val birthdate: LocalDate? = null,
)

/**
 * DTO for PUT /profiles response.
 *
 * The fields are never null, returns after updating.
 */
data class PutProfilesResponse(
    val username: String,
    val email: String,
    val birthdate: LocalDate,
    val avatar: String?,
)
