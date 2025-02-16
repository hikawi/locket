package dev.frilly.locket.configuration

import dev.frilly.locket.component.AuthEntrypoint
import dev.frilly.locket.component.JwtAuthenticationFilter
import dev.frilly.locket.service.JwtService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Configuration file for Spring Security over HTTP.
 */
@Configuration
@EnableWebSecurity
class SpringSecurityConfig {

    @Autowired
    private lateinit var authEntrypoint: AuthEntrypoint

    @Autowired
    private lateinit var deniedHandler: AccessDeniedHandler

    /**
     * A bean that represents a security filter chain.
     *
     * It allows unauthenticated requests to routes /login, /register and
     * logouts, but any other route has to be authenticated.
     */
    @Bean
    fun securityFilterChain(
        sec: HttpSecurity,
        jwt: JwtAuthenticationFilter
    ): SecurityFilterChain {
        return sec
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/login", "/register", "/").permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint(authEntrypoint)
                    .accessDeniedHandler(deniedHandler)
            }
            .addFilterBefore(
                jwt,
                UsernamePasswordAuthenticationFilter::class.java
            )
            .build()
    }

    /**
     * A bean that holds an instance of the password encoder.
     */
    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    /**
     * A bean that provides the JWT authentication service.
     */
    @Bean
    fun jwtService(): JwtService {
        return JwtService()
    }

}