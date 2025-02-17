package dev.frilly.locket.repo

import dev.frilly.locket.data.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

/**
 * A CRUD repository for managing users.
 */
@Repository
interface UserRepository : JpaRepository<User, Long> {

    @Query("select u from User u where lower(u.username) = lower(:username)")
    fun findByUsername(username: String): Optional<User>

    @Query("select u from User u where lower(u.email) = lower(:email)")
    fun findByEmail(email: String): Optional<User>

    @Query("select u from User u where lower(u.email) = lower(:email) or lower(u.username) = lower(:username)")
    fun findByUsernameOrEmail(username: String, email: String): Optional<User>

}