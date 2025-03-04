package dev.frilly.locket.data;

import dev.frilly.locket.dto.res.UserResponse;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Entity model class for a user in the system.
 */
@Entity
@Table(name = "users")
public final class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, unique = true, length = 32)
  private String username;

  @Column(nullable = false)
  private String password;

  private LocalDate birthdate = null;

  @Enumerated(value = EnumType.STRING)
  @Column(nullable = false)
  private Role role = Role.USER;

  @Column(name = "avatar_url")
  private String avatarUrl = null;

  public long id() {
    return id;
  }

  public String email() {
    return email;
  }

  public User setEmail(String email) {
    this.email = email;
    return this;
  }

  public String username() {
    return username;
  }

  public User setUsername(String username) {
    this.username = username;
    return this;
  }

  public String password() {
    return password;
  }

  public User setPassword(String password) {
    this.password = password;
    return this;
  }

  public LocalDate birthdate() {
    return birthdate;
  }

  public User setBirthdate(LocalDate birthdate) {
    this.birthdate = birthdate;
    return this;
  }

  public Role role() {
    return role;
  }

  public User setRole(Role role) {
    this.role = role;
    return this;
  }

  public String avatarUrl() {
    return avatarUrl;
  }

  public User setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
    return this;
  }

  public UserResponse makeResponse() {
    return new UserResponse(id, username, email, avatarUrl, birthdate);
  }

  /**
   * The role for a user, in case we need some administrative routes.
   */
  public enum Role {

    USER, MODERATOR, ADMINISTRATOR

  }

}
