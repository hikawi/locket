package dev.frilly.locket.data

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Represents a friend request.
 *
 * A table has:
 * - ID (Serial) primary key
 * - SENDER_ID (Serial) references users
 * - RECEIVER_ID (Serial) references users
 * - TIME (Time)
 */
@Entity
@Table(
    name = "friend_requests",
    uniqueConstraints = [UniqueConstraint(
        columnNames = ["sender_id", "receiver_id"],
    )]
)
data class FriendRequest(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    val receiver: User,

    @Column(nullable = false)
    val time: LocalDateTime = LocalDateTime.now(),
)
