package dev.frilly.locket.data

import jakarta.persistence.*

/**
 * Represents a group of friends, allowing to share pictures with each other.
 *
 * A table with columns (ID, name)
 */
@Entity
@Table(name = "groups")
data class Group(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(nullable = false)
    var name: String,

    @ManyToMany(cascade = [CascadeType.ALL], mappedBy = "groups")
    val users: Set<User> = emptySet(),
)
