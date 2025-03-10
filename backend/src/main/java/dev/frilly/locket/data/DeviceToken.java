package dev.frilly.locket.data;

import jakarta.persistence.*;

/**
 * Represents a device token. We split this out of {@link User} because a
 * user can log in to many devices with the same account, but all devices
 * need to have the same notifications, but they have different FCM tokens.
 */
@Entity
@Table(name = "device_tokens", indexes = {@Index(columnList = "token")})
public class DeviceToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(nullable = false)
  private String token;

  @JoinColumn(nullable = false, name = "user_id")
  @ManyToOne
  private User user;

  public long id() {
    return id;
  }

  public String token() {
    return token;
  }

  public DeviceToken setToken(String token) {
    this.token = token;
    return this;
  }

  public User user() {
    return user;
  }

  public DeviceToken setUser(User user) {
    this.user = user;
    return this;
  }

}
