package dev.frilly.locket.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

/**
 * Configuration file for Spring Security over HTTP.
 */
@Configuration
@EnableWebSecurity
class SpringSecurityConfig {

    /**
     * A bean that represents a security filter chain.
     *
     * It allows unauthenticated requests to routes /login, /register and
     * logouts, but any other route has to be authenticated.
     */
    @Bean
    fun securityFilterChain(sec: HttpSecurity): SecurityFilterChain {
        return sec
            .httpBasic { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/login", "/register", "/").permitAll()
                    .anyRequest().authenticated()
            }
            .logout { it.permitAll() }
            .build()
    }

}