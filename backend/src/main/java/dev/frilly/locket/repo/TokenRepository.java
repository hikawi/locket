package dev.frilly.locket.repo;

import dev.frilly.locket.data.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * CRUD repository for device tokens.
 */
public interface TokenRepository extends JpaRepository<DeviceToken, Long> {

  Optional<DeviceToken> findByToken(final String token);

}
