package dev.frilly.locket.controller.dto

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
