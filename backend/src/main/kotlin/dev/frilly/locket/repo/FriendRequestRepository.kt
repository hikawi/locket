package dev.frilly.locket.repo

import dev.frilly.locket.data.FriendRequest
import org.springframework.data.jpa.repository.JpaRepository

/**
 * CRUD Repository for handling friend requests.
 */
interface FriendRequestRepository : JpaRepository<FriendRequest, Long>
