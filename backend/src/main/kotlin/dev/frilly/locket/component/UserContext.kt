package dev.frilly.locket.component

import dev.frilly.locket.data.User
import org.springframework.stereotype.Component

/**
 * The authentication context for any request.
 */
@Component
object UserContext {

    val user: ThreadLocal<User?> = ThreadLocal()

}