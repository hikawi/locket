package dev.frilly.locket.controller

import dev.frilly.locket.data.User
import dev.frilly.locket.repo.UserRepository
import dev.frilly.locket.service.JwtService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import kotlin.jvm.optionals.getOrNull

/**
 * The REST controller for handling logging in and registering.
 */
@RestController
class AccountsController {

    data class LoginBodyRequest(val username: String, val password: String)
    data class LoginBodyResponse(val token: String)
    data class RegisterBodyRequest(val username: String, val password: String)

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    @Autowired
    private lateinit var userRepository: UserRepository

    /**
     * Handles the POST login request. Accepts body { username, password }.
     */
    @PostMapping("/login")
    fun postLogin(@RequestBody body: LoginBodyRequest): ResponseEntity<LoginBodyResponse> {
        val user = userRepository.findByUsername(body.username).getOrNull()
            ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        if (!bCryptPasswordEncoder.matches(body.password, user.password))
            return ResponseEntity(HttpStatus.FORBIDDEN)

        return ResponseEntity.ok(LoginBodyResponse(jwtService.encode(user.id)))
    }

    /**
     * Handles the POST register request. Accepts body { username, password }.
     */
    @PostMapping("/register")
    fun postRegister(@RequestBody body: RegisterBodyRequest): ResponseEntity<LoginBodyResponse> {
        if (userRepository.findByUsername(body.username).isPresent)
            return ResponseEntity(HttpStatus.CONFLICT)

        val user = User(
            id = 0,
            name = "",
            username = body.username,
            password = bCryptPasswordEncoder.encode(body.password),
        )
        userRepository.save(user)
        return ResponseEntity.ok(LoginBodyResponse(jwtService.encode(user.id)))
    }

}
