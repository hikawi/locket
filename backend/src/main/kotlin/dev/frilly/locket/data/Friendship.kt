package dev.frilly.locket.data

import jakarta.persistence.*
import java.io.Serializable

/**
 * Composite primary key for the friendship table.
 */
@Embeddable
data class FriendshipCompositeKey(
    var user1: Long,
    var user2: Long,
) : Serializable {

    init {
        val temp = user1
        if (user1 > user2) {
            user1 = user2
            user2 = temp
        }
    }

}

/**
 * A relationship between two users.
 *
 * A table has:
 * - USER1 (Serial) primary key
 * - USER2 (Serial) primary key
 */
@Entity
@Table(
    name = "friendships",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user1", "user2"])],
)
data class Friendship(
    @EmbeddedId
    val id: FriendshipCompositeKey,

    @ManyToOne
    @MapsId("user1")
    @JoinColumn(name = "user1", nullable = false)
    val user1: User,

    @ManyToOne
    @MapsId("user2")
    @JoinColumn(name = "user2", nullable = false)
    val user2: User,
)
