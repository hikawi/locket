package dev.frilly.locket.repo

import dev.frilly.locket.data.Message
import org.springframework.data.jpa.repository.JpaRepository

/**
 * CRUD repository for messages
 */
interface MessagesRepository : JpaRepository<Message, Long>