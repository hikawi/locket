package dev.frilly.locket.repo

import dev.frilly.locket.data.Post
import dev.frilly.locket.data.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

/**
 * A CRUD Repository for managing posts.
 */
interface PostsRepository : JpaRepository<Post, Long> {

    @Query(
        "select p from Post p where p.time >= :from and p.time <= :to and " +
                "(:viewer member of p.viewers or p.user = :viewer)"
    )
    fun getPostsInRange(viewer: User, from: LocalDateTime, to: LocalDateTime):
            List<Post>

}
