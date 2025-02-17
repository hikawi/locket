package dev.frilly.locket.controller

import dev.frilly.locket.controller.dto.LoginPostRequest
import dev.frilly.locket.controller.dto.LoginPostResponse
import dev.frilly.locket.controller.dto.RegisterPostRequest
import dev.frilly.locket.data.User
import dev.frilly.locket.repo.UserRepository
import dev.frilly.locket.service.JwtService
import jakarta.validation.Valid
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
    fun postLogin(@Valid @RequestBody body: LoginPostRequest):
            ResponseEntity<LoginPostResponse> {
        val user = userRepository.findByUsername(body.username).getOrNull()
            ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        if (!bCryptPasswordEncoder.matches(body.password, user.password))
            return ResponseEntity(HttpStatus.FORBIDDEN)

        return ResponseEntity.ok(LoginPostResponse(jwtService.encode(user.id)))
    }

    /**
     * Handles the POST register request.
     */
    @PostMapping("/register")
    fun postRegister(@Valid @RequestBody body: RegisterPostRequest):
            ResponseEntity<LoginPostResponse> {
        if (userRepository.findByUsernameOrEmail(
                body.username,
                body.email
            ).isPresent
        )
            return ResponseEntity(HttpStatus.CONFLICT)

        val user = User(
            id = 0,
            email = body.email,
            username = body.username,
            birthdate = body.birthdate,
            password = bCryptPasswordEncoder.encode(body.password),
        )
        userRepository.save(user)
        return ResponseEntity.ok(LoginPostResponse(jwtService.encode(user.id)))
    }

}
