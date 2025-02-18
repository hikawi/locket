package dev.frilly.locket.repo

import dev.frilly.locket.data.Post
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A CRUD Repository for managing posts.
 */
interface PostsRepository : JpaRepository<Post, Long>