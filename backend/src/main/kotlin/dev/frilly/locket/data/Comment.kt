package dev.frilly.locket.data

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Represents a comment under a post.
 *
 * The table would have (id, user_id, post_id, content, time)
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

    @Column(nullable = false)
    val content: String,

    @Column(nullable = false)
    val time: LocalDateTime = LocalDateTime.now(),
)
