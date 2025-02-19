package dev.frilly.locket.repo

import dev.frilly.locket.data.Friendship
import dev.frilly.locket.data.FriendshipCompositeKey
import dev.frilly.locket.data.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

/**
 * CRUD repository for [[Friendship]].
 */
interface FriendshipRepository :
    JpaRepository<Friendship, FriendshipCompositeKey> {

    @Query(
        "select f from Friendship f where f.user1 = :user or f.user2 = :user"
    )
    fun findByUser(user: User, pageable: Pageable): Page<Friendship>

    @Query(
        "select f from Friendship f where (f.user1 = :user1 and f.user2 = " +
                ":user2) or (f.user2 = :user1 and f.user1 = :user2)"
    )
    fun findWithTwoUsers(user1: User, user2: User): Optional<Friendship>

}
