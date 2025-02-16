package dev.frilly.locket.data

import jakarta.persistence.*
import java.io.Serializable

/**
 * Represents a type of reaction a user can react to another user's Locket post.
 */
enum class ReactionType {

    LIKE,
    LOVE,
    FUNNY,
    SAD,
    ANGRY,

}

/**
 * Composite primary key for [[Reaction]].
 */
@Embeddable
data class ReactionId(val postId: Long, val userId: Long) : Serializable

/**
 * Represents a reaction to a post. Should this be separate from [[Comment]]?
 *
 * The table has:
 * - POST_ID (Serial) primary key references post
 * - USER_ID (Serial) primary key references user
 * - REACTION (Text) enum type, check [[ReactionType]]
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
