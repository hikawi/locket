package dev.frilly.locket.data

import jakarta.persistence.*
import java.io.Serializable

/**
 * Composite primary key for [[Reaction]].
 */
@Embeddable
data class ReactionId(val postId: Long, val userId: Long) : Serializable

/**
 * Represents a reaction to a post.
 *
 * The table has (**post_id**, **user_id**, reaction_type)
 */
@Entity
@Table(name = "reactions")
data class Reaction(
    @EmbeddedId
    val id: ReactionId,

    @ManyToOne(cascade = [CascadeType.ALL])
    @MapsId("postId")
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post,

    @ManyToOne(cascade = [CascadeType.ALL])
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val reaction: ReactionType = ReactionType.LIKE,
)
