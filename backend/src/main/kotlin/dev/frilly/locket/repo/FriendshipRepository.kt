package dev.frilly.locket.repo

import dev.frilly.locket.data.Friendship
import dev.frilly.locket.data.FriendshipCompositeKey
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * CRUD repository for [[Friendship]].
 */
interface FriendshipRepository :
    JpaRepository<Friendship, FriendshipCompositeKey> {

    @Query("select f from Friendship f where f.user1.id = :user")
    fun findAllByUser(user: Long, pageable: Pageable): Page<Friendship>

}
