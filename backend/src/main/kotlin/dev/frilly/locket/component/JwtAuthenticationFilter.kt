package dev.frilly.locket.component

import dev.frilly.locket.repo.UserRepository
import dev.frilly.locket.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import kotlin.jvm.optionals.getOrNull

/**
 * A component on the authentication pipeline, using JWT service.
 */
@Component
class JwtAuthenticationFilter : OncePerRequestFilter() {

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var userRepository: UserRepository

    private fun extractToken(req: HttpServletRequest): Long? {
        try {
            val authHeader = req.getHeader("Authorization")
            val match = Regex("Bearer (.+)").matchEntire(authHeader)
                ?: return null

            val token = match.groups[1]
            return jwtService.decode(token!!.value)
        } catch (ex: Exception) {
            return null
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val excludedPaths = listOf("/login", "/register", "/", "/profiles")
        return excludedPaths.any { request.requestURI.equals(it, true) }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val user = extractToken(request)?.let { userRepository.findById(it) }
            ?.getOrNull()

        if (user == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }

        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken.authenticated(
                user,
                null,
                listOf(SimpleGrantedAuthority(user.role.toString())),
            )
        filterChain.doFilter(request, response)
    }

}