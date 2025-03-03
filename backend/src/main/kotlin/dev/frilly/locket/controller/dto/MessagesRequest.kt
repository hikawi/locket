package dev.frilly.locket.controller.dto

/**
 * An abstract version of a message.
 */
data class AbstractMessage(
    val id: Long,
    val sender: AbstractUser,
)
