package dev.frilly.locket.repo

import dev.frilly.locket.data.FriendRequest
import dev.frilly.locket.data.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

/**
 * CRUD Repository for handling friend requests.
 */
interface FriendRequestRepository : JpaRepository<FriendRequest, Long> {

    fun findAllByReceiver(receiver: User): List<FriendRequest>

    @Query(
        "select r from FriendRequest r where r.sender = :sender and r" +
                ".receiver = :receiver"
    )
    fun findBySenderAndReceiver(sender: User, receiver: User):
            Optional<FriendRequest>

}
