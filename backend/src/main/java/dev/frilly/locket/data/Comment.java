package dev.frilly.locket.data;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity model for a comment under a post.
 */
@Entity
@Table(name = "comments")
public final class Comment {

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private final User user;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  private final Post post;

  @Column(nullable = false, columnDefinition = "text")
  private final String content;

  @Column(nullable = false)
  private final LocalDateTime time = LocalDateTime.now();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  public Comment(User user, Post post, String content) {
    this.user    = user;
    this.post    = post;
    this.content = content;
  }

  public long id() {
    return id;
  }

  public User user() {
    return user;
  }

  public Post post() {
    return post;
  }

  public String content() {
    return content;
  }

  public LocalDateTime time() {
    return time;
  }

}
