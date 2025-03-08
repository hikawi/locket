package dev.frilly.locket.component;

import dev.frilly.locket.repo.UserRepository;
import dev.frilly.locket.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A filter that is ran once per request (hence the name) that checks for
 * valid JWTs, and saves them in the context holder for main controllers to
 * access.
 */
@Component
public final class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Autowired
  private JwtService jwtService;

  @Autowired
  private UserRepository userRepo;

  @Override
  protected void doFilterInternal(
      @NotNull final HttpServletRequest request,
      @NotNull final HttpServletResponse response,
      @NotNull final FilterChain filterChain
  ) throws ServletException, IOException {
    System.out.println("JWT Authentication Filter");
    try {
      final var userId = extractToken(request);
      final var user   = userRepo.findById(userId);

      if (user.isPresent()) {
        final var context = SecurityContextHolder.getContext();
        final var auth = UsernamePasswordAuthenticationToken.authenticated(
            user.get(), null,
            List.of(new SimpleGrantedAuthority(user.get().role().toString())));
        context.setAuthentication(auth);
      }
    } catch (JwtException ex) {
      // Ignored
    }

    filterChain.doFilter(request, response);
  }

  private long extractToken(final HttpServletRequest req) throws JwtException {
    final var header = req.getHeader("Authorization");
    if (header == null) {
      return -1;
    }

    final var matcher = Pattern.compile("Bearer (.*)").matcher(header);

    if (!matcher.matches()) {
      return -1;
    }

    return jwtService.decode(matcher.group(1));
  }

}
