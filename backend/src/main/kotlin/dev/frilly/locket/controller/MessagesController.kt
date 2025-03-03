package dev.frilly.locket.controller

import dev.frilly.locket.repo.MessagesRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for handling sending and receiving messages.
 */
@RestController
class MessagesController {

    @Autowired
    private lateinit var messagesRepo: MessagesRepository

    /**
     * Retrieves a list of unseen messages.
     */
    @GetMapping("/messages")
    fun getMessages() {

    }

    /**
     * Sends a message
     */
    @PostMapping("/messages")
    fun postMessage() {

    }

}
