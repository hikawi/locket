package dev.frilly.locket.service

import dev.frilly.locket.repo.UserRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

/**
 * The component that provides the JWT system to other components.
 */
@Service
class JwtService {

    @Value("\${jwt.secret}")
    lateinit var jwtSecret: String

    @Value("\${jwt.expiration}")
    var jwtExpiration: Long = 0

    @Autowired
    lateinit var userRepository: UserRepository

    lateinit var key: SecretKey

    /**
     * After injecting values to those two fields, can we setup the key.
     */
    @PostConstruct
    fun init() {
        key = Keys.hmacShaKeyFor(jwtSecret.encodeToByteArray())
    }

    /**
     * Encodes a user object.
     */
    fun encode(id: Long): String {
        return Jwts.builder()
            .claim("id", id)
            .issuedAt(Date())
            .expiration(Date(Date().time + jwtExpiration))
            .signWith(key)
            .compact()
    }

    /**
     * Decodes a string back to a user.
     */
    fun decode(s: String): Long {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(s)
        return claims.payload.get("id", Integer::class.java).toLong()
    }

    /**
     * Checks if a token is valid.
     */
    fun isValid(s: String): Boolean {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(s)
            return true
        } catch (_: Exception) {
            return false
        }
    }

}