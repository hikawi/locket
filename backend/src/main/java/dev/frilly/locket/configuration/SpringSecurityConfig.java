package dev.frilly.locket.configuration;

import dev.frilly.locket.component.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration file for Spring Security. Spring Security is the pipeline
 * that handles which requests should be checked, and which requests should
 * be denied.
 */
@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

  /**
   * A bean that represents a security filter chain.
   * <p>
   * It allows unauthenticated requests to routes /login, /register and
   * logouts, but any other route has to be authenticated.
   *
   * @return A security filter chain
   */
  @Bean
  public SecurityFilterChain filterChain(
      final HttpSecurity sec,
      final JwtAuthenticationFilter filter
  ) throws Exception {
    return sec.httpBasic(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            it -> it.requestMatchers("/login", "/register", "/", "/google")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/profiles")
                .permitAll()
                .anyRequest()
                .authenticated())
        .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  /**
   * A bean that holds an instance of the password encoder.
   *
   * @return an encoder using bcrypt
   */
  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}
