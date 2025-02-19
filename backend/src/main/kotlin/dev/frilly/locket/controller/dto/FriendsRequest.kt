package dev.frilly.locket.controller.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern

data class AbstractUser(
    val username: String,
    val avatar: String?,
)

/**
 * Response for getting /friends.
 *
 * Contains a list of friends, paginated.
 */
data class GetFriendsResponse(
    val total: Long,
    val totalPages: Int,
    val results: List<String>, // list of usernames
)

/**
 * Response for getting /requests.
 *
 * Contains a list of all friend requests up until now.
 */
data class GetRequestsResponse(
    val results: List<AbstractUser>,
)

/**
 * Request schema for posting /requests.
 *
 * Contains the username that the user wants to send a friend request to.
 */
data class PostRequestsRequest(
    @NotEmpty
    @Pattern(regexp = "[a-zA-Z-_][a-zA-Z0-9-_]+")
    val username: String,
)
