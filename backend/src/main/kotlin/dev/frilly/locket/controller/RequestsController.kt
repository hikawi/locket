package dev.frilly.locket.controller

import dev.frilly.locket.controller.dto.AbstractUser
import dev.frilly.locket.controller.dto.GetRequestsResponse
import dev.frilly.locket.controller.dto.PostRequestsRequest
import dev.frilly.locket.data.FriendRequest
import dev.frilly.locket.data.Friendship
import dev.frilly.locket.data.FriendshipCompositeKey
import dev.frilly.locket.data.User
import dev.frilly.locket.repo.FriendRequestRepository
import dev.frilly.locket.repo.FriendshipRepository
import dev.frilly.locket.repo.UserRepository
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import kotlin.jvm.optionals.getOrNull

/**
 * Controller handler for friend requests (/requests).
 */
@RestController
class RequestsController {

    @Autowired
    private lateinit var requestRepository: FriendRequestRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var friendshipRepository: FriendshipRepository

    /**
     * GET /requests.
     * Retrieves a list of my friend requests.
     */
    @GetMapping("/requests")
    fun getRequests(): GetRequestsResponse {
        val auth = SecurityContextHolder.getContext().authentication
        val user = auth.principal as User

        return GetRequestsResponse(
            results = requestRepository.findAllByReceiver(user).map {
                AbstractUser(
                    it.sender.id,
                    it.sender.username,
                    it.sender.avatarUrl,
                )
            }
        )
    }

    /**
     * POST /requests.
     * Sends a new request.
     */
    @PostMapping("/requests")
    fun postRequests(@Valid @RequestBody body: PostRequestsRequest)
            : ResponseEntity<PostRequestsRequest> {
        val auth = SecurityContextHolder.getContext().authentication
        val user = auth.principal as User
        val target = userRepository.findByUsername(body.username).getOrNull()
            ?: return ResponseEntity.notFound().build()

        if (user.id == target.id) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        if (requestRepository.findBySenderAndReceiver(user, target).isPresent) {
            return ResponseEntity(HttpStatus.CONFLICT)
        }

        // If the other already sent a request, we can now "accept" it.
        val req = requestRepository.findBySenderAndReceiver(target, user)
        if (req.isPresent) {
            requestRepository.delete(req.get())

            val friendship =
                if (user.id < target.id)
                    Friendship(
                        id = FriendshipCompositeKey(user.id, target.id),
                        user1 = user,
                        user2 = target,
                    )
                else
                    Friendship(
                        id = FriendshipCompositeKey(target.id, user.id),
                        user1 = target,
                        user2 = user,
                    )
            friendshipRepository.save(friendship)
            return ResponseEntity.noContent().build()
        }

        val obj = FriendRequest(
            id = 0,
            sender = user,
            receiver = target,
        )
        requestRepository.save(obj)
        return ResponseEntity.ok(PostRequestsRequest(target.username))
    }

    /**
     * DELETE /requests
     * Delete a friend request sent.
     */
    @DeleteMapping("/requests")
    fun deleteRequests(@Valid @RequestBody body: PostRequestsRequest)
            : ResponseEntity<PostRequestsRequest> {
        val auth = SecurityContextHolder.getContext().authentication
        val user = auth.principal as User
        val target = userRepository.findByUsername(body.username).getOrNull()
            ?: return ResponseEntity.notFound().build()

        val obj =
            requestRepository.findBySenderAndReceiver(user, target).getOrNull()
                ?: return ResponseEntity.noContent().build()
        requestRepository.delete(obj)
        return ResponseEntity.ok(PostRequestsRequest(target.username))
    }

}
