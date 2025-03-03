package dev.frilly.locket.controller

import dev.frilly.locket.controller.dto.AbstractUser
import dev.frilly.locket.controller.dto.DeleteFriendsRequest
import dev.frilly.locket.controller.dto.GetFriendsResponse
import dev.frilly.locket.data.User
import dev.frilly.locket.repo.FriendshipRepository
import dev.frilly.locket.repo.UserRepository
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import kotlin.jvm.optionals.getOrNull

/**
 * The controller for handling routes /friends.
 */
@RestController
class FriendsController {

    @Autowired
    private lateinit var friendshipRepository: FriendshipRepository

    @Autowired
    private lateinit var userRepository: UserRepository

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

        val query = friendshipRepository.findByUser(
            user,
            PageRequest.of(
                page,
                perPage,
                Sort.by("user1")
            )
        )

        return GetFriendsResponse(
            total = query.totalElements,
            totalPages = query.totalPages,
            results = query.content.map {
                val other = if (it.user1.id == user.id) it.user2
                else it.user1
                AbstractUser(other.id, other.username, other.avatarUrl)
            },
        )
    }

    /**
     * DELETE /friends.
     *
     * Remove a friend.
     */
    @DeleteMapping("/friends")
    fun deleteFriends(@Valid @RequestBody body: DeleteFriendsRequest):
            ResponseEntity<AbstractUser> {
        val user =
            SecurityContextHolder.getContext().authentication.principal as User
        val target = userRepository.findByUsername(body.username).getOrNull()
            ?: return ResponseEntity.notFound().build()

        val friendship = friendshipRepository.findWithTwoUsers(user, target)
            .getOrNull() ?: return ResponseEntity.noContent().build()

        friendshipRepository.delete(friendship)
        return ResponseEntity.ok(
            AbstractUser(
                target.id,
                target.username,
                target.avatarUrl
            )
        )
    }

}
