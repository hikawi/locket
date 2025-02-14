package dev.frilly.locket.data

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Represents a picture share.
 *
 * A table with columns (ID, user_id, group_id, image_link, message, time)
 */
@Entity
@Table(name = "posts")
data class Post(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    val group: Group,

    @Column(name = "image_link", nullable = false)
    val imageLink: String,

    @Column(nullable = true)
    val message: String? = null,

    @Column(nullable = false)
    val time: LocalDateTime = LocalDateTime.now(),

    @OneToMany(
        mappedBy = "post",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    val comments: MutableSet<Comment> = mutableSetOf(),

    @OneToMany(
        mappedBy = "post",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    val reactions: MutableSet<Reaction> = mutableSetOf(),
)
