package dev.frilly.locket.component;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * The component that provides some random-related beans.
 */
@Component
public final class RandomComponent {

  @Bean
  public SecureRandom secureRandom() {
    return new SecureRandom();
  }

}
