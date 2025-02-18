package dev.frilly.locket.component

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

/**
 * Customized authentication entrypoint to prevent it from returning wrong
 * status codes.
 */
@Component
class AuthEntrypoint : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authException: AuthenticationException?
    ) {
        response?.sendError(
            HttpServletResponse.SC_UNAUTHORIZED,
            "Unauthorized"
        )
    }

}