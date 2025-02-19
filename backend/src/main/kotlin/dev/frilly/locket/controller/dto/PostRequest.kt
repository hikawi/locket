package dev.frilly.locket.controller.dto

import java.time.LocalDateTime

data class AbstractPost(
    val id: Long,
    val poster: AbstractUser,
    val image: String,
    val message: String?,
    val time: LocalDateTime,
)

data class GetPostsResponse(
    val total: Int,
    val results: List<AbstractPost>,
)

data class PostPostsRequest(
    val message: String = "",
)
