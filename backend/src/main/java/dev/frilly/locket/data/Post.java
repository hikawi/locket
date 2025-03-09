package dev.frilly.locket.data;

import dev.frilly.locket.dto.res.PostResponse;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity model for the Post table.
 */
@Entity
@Table(name = "posts")
public final class Post {

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private final User user;

  @ManyToMany
  @JoinTable(
      name = "post_viewers", joinColumns = {@JoinColumn(name = "post_id")},
      inverseJoinColumns = {@JoinColumn(name = "user_id")}
  )
  private final Set<User> viewers = new HashSet<>();

  @Column(nullable = false)
  private final LocalDateTime time = LocalDateTime.now();

  @OneToMany(
      mappedBy = "post", cascade = {CascadeType.ALL}, orphanRemoval = true
  )
  private final Set<Comment> comments = new HashSet<>();

  @OneToMany(
      mappedBy = "post", cascade = {CascadeType.ALL}, orphanRemoval = true
  )
  private final Set<Reaction> reactions = new HashSet<>();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "image_link", nullable = false)
  private String imageLink;

  @Column(columnDefinition = "text")
  private String message;

  public Post(User user, String imageLink, String message) {
    this.user      = user;
    this.imageLink = imageLink;
    this.message   = message;
  }

  public long id() {
    return id;
  }

  public User user() {
    return user;
  }

  public Set<User> viewers() {
    return viewers;
  }

  public String imageLink() {
    return imageLink;
  }

  public Post setImageLink(String imageLink) {
    this.imageLink = imageLink;
    return this;
  }

  public String message() {
    return message;
  }

  public Post setMessage(String message) {
    this.message = message;
    return this;
  }

  public LocalDateTime time() {
    return time;
  }

  public PostResponse makeResponse() {
    return new PostResponse(id, user.makeResponse(), imageLink, message, time);
  }

  public Set<Comment> comments() {
    return comments;
  }

  public Set<Reaction> reactions() {
    return reactions;
  }

}
