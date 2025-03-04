package dev.frilly.locket.service;

import dev.frilly.locket.repo.UserRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * The component that provides access to the Java-JWT library.
 */
@Service
public final class JwtService {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.expiration}")
  private long jwtExpiration;

  @Autowired
  private UserRepository userRepo;

  private SecretKey key;

  /**
   * After injecting values to those two fields, we can setup the key by
   * grabbing the encoded array of the JWT Secret.
   */
  @PostConstruct
  public void init() {
    key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Encodes a user's ID.
   *
   * @param id the id of the user
   *
   * @return the encoded JWT
   */
  public String encode(final long id) {
    return Jwts.builder()
        .claim("id", id)
        .issuedAt(new Date())
        .expiration(new Date(new Date().getTime() + jwtExpiration))
        .signWith(key)
        .compact();
  }

  /**
   * Decodes a token back to a user's ID, if possible.
   *
   * @param token the token
   *
   * @return the user's ID
   *
   * @throws io.jsonwebtoken.JwtException if the token is invalid
   */
  public long decode(final String token) throws JwtException {
    final var claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token);
    return claims.getPayload().get("id", Integer.class).longValue();
  }

}
