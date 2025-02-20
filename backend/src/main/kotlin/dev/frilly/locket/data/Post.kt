package dev.frilly.locket.data

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Represents a picture share. A picture can be shared with a group of friends.
 *
 * A table with columns:
 * - ID (Serial) primary key
 * - USER_ID (Serial) the uploader references users
 * - IMAGE_LINK (Text) the link to the hosted image
 * - MESSAGE (Text) the message with the image
 * - TIME (Time) when the post was uploaded
 *
 * This also implicitly creates a table "post_viewers", to define if the user
 * can see a post or not:
 * - POST_ID (Serial) references posts
 * - USER_ID (Serial) references users
 */
@Entity
@Table(name = "posts")
data class Post(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToMany
    @JoinTable(
        name = "post_viewers",
        joinColumns = [JoinColumn(name = "post_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")],
    )
    var viewers: MutableSet<User> = mutableSetOf(),

    @Column(name = "image_link", nullable = false)
    var imageLink: String,

    @Column(nullable = true, columnDefinition = "text")
    var message: String? = null,

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
