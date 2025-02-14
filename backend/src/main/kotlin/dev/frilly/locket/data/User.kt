package dev.frilly.locket.data

import jakarta.persistence.*

/**
 * Represents a user in the system.
 *
 * A table that includes (ID, name, username, password, role)
 */
@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(nullable = true)
    var name: String,

    @Column(nullable = false, unique = true, length = 32)
    var username: String,

    @Column(nullable = false, unique = false)
    var password: String,

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole = UserRole.USER,

    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(
        name = "group_memberships",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "group_id")],
    )
    val groups: Set<Group> = emptySet(),
)