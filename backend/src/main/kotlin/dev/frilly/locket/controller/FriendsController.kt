package dev.frilly.locket.controller

import dev.frilly.locket.controller.dto.GetFriendsResponse
import dev.frilly.locket.data.User
import dev.frilly.locket.repo.FriendshipRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * The controller for handling routes /friends.
 */
@RestController
class FriendsController {

    @Autowired
    private lateinit var friendshipRepository: FriendshipRepository

    /**
     * GET /friends.
     *
     * Retrieves a list of my friends, for example who can I share with.
     */
    @GetMapping("/friends")
    fun getFriends(
        @RequestParam("page") page: Int = 0,
        @RequestParam("per_page") perPage: Int = 20
    ): GetFriendsResponse {
        val user =
            SecurityContextHolder.getContext().authentication.principal as User

        val query = friendshipRepository.findAllByUser(
            user.id,
            PageRequest.of(
                page,
                perPage,
                Sort.by("user1")
            )
        )

        return GetFriendsResponse(
            total = query.totalElements,
            totalPages = query.totalPages,
            results = query.content.map { it.user2.username },
        )
    }

}
