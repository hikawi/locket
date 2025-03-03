package dev.frilly.locket.controller

import dev.frilly.locket.controller.dto.Profile
import dev.frilly.locket.controller.dto.PutProfilesRequest
import dev.frilly.locket.data.User
import dev.frilly.locket.repo.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import kotlin.jvm.optionals.getOrNull

/**
 * Controller for routes on /profiles, allows retrieving and updating user
 * accounts.
 */
@RestController
class ProfilesController {

    @Autowired
    private lateinit var userRepo: UserRepository

    @Autowired
    private lateinit var bcrypt: BCryptPasswordEncoder

    @GetMapping("/profiles")
    fun getProfile(
        @RequestParam username: String = ""
    ): ResponseEntity<Profile> {
        val auth = SecurityContextHolder.getContext().authentication

        if (auth.principal is User && username.isBlank()) {
            val user = auth.principal as User
            return ResponseEntity.ok(
                Profile(
                    username = user.username,
                    email = user.email,
                    birthdate = user.birthdate,
                    avatar = user.avatarUrl,
                ),
            )
        } else {
            val target = userRepo.findByUsername(username).getOrNull()
                ?: return ResponseEntity.notFound().build()
            return ResponseEntity.ok(
                Profile(
                    username = target.username,
                    email = target.email,
                    birthdate = target.birthdate,
                    avatar = target.avatarUrl,
                ),
            )
        }
    }

    /**
     * Controller for PUT /profiles.
     *
     * This endpoint is for updating profiles.
     */
    @PutMapping("/profiles")
    fun putProfile(@RequestBody body: PutProfilesRequest)
            : ResponseEntity<Profile> {
        val auth = SecurityContextHolder.getContext().authentication
        val user = auth.principal as User

        if (body.username != null) {
            val usernameNeighbor = userRepo.findByUsername(body.username)
                .getOrNull()
            if (usernameNeighbor != null && usernameNeighbor.id != user.id) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build()
            }

            user.username = body.username
        }

        if (body.password != null) {
            user.password = bcrypt.encode(body.password)
        }

        if (body.email != null) {
            val emailNeighbor = userRepo.findByEmail(body.email).getOrNull()
            if (emailNeighbor != null && emailNeighbor.id != user.id) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build()
            }

            user.email = body.email
        }

        if (body.birthdate != null) {
            user.birthdate = body.birthdate
        }

        userRepo.save(user)
        return ResponseEntity.ok(
            Profile(
                username = user.username,
                email = user.email,
                birthdate = user.birthdate,
                avatar = user.avatarUrl,
            ),
        )
    }

}