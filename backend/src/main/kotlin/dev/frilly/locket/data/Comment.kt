package dev.frilly.locket.data

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Represents a comment under a post. Should this be separated from messages?
 *
 * The table has:
 * - ID (Serial) primary key
 * - USER_ID (Serial) references users
 * - POST_ID (Serial) references posts
 * - CONTENT (Text) the comment
 * - TIME (Time) when the comment was posted
 */
@Entity
@Table(name = "comments")
data class Comment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post,

    @Column(nullable = false, columnDefinition = "text")
    val content: String,

    @Column(nullable = false)
    val time: LocalDateTime = LocalDateTime.now(),
)
