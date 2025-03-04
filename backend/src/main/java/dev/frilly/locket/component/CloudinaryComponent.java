package dev.frilly.locket.component;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Represents a wrapper for a Cloudinary instance. This component's main job
 * is to export a Cloudinary instance as a bean.
 */
@Component
public final class CloudinaryComponent {

  @Value("${secrets.cloudinary}")
  private String cloudinaryUrl;

  /**
   * Creates a bean of type {@link Cloudinary}.
   *
   * @return the bean {@link Cloudinary}
   */
  @Bean
  public Cloudinary cloudinaryInstance() {
    return new Cloudinary(cloudinaryUrl);
  }

}
