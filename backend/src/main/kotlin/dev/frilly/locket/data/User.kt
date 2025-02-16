package dev.frilly.locket.data

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * A user role, in case we need for administrative events.
 */
enum class UserRole {

    USER,
    MODERATOR,
    ADMINISTRATOR,

}

/**
 * Represents a user in the system.
 *
 * A table that includes:
 * - ID (Serial) primary key
 * - EMAIL (Text) unique not null
 * - USERNAME (Text) unique not null
 * - PASSWORD (Text) not null
 * - ROLE (Text) not null
 *
 * This also implicitly creates a new table called "friendship", which has:
 * - USER1 foreign key references users
 * - USER2 foreign key references users
 */
@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false, unique = true, length = 32)
    var username: String,

    @Column(nullable = false, unique = false)
    var password: String,

    @Column(nullable = false)
    val birthdate: LocalDateTime,

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole = UserRole.USER,

    @OneToMany(mappedBy = "user1", cascade = [CascadeType.ALL])
    val relationships: Set<Friendship> = emptySet(),
)