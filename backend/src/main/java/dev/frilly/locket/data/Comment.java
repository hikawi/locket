package dev.frilly.locket.data;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity model for a comment under a post.
 */
@Entity
@Table(name = "comments")
public final class Comment {

  @Column(nullable = false)
  private final LocalDateTime time;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @Column(nullable = false, columnDefinition = "text")
  private String content;
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  public Comment(User user, Post post, String content) {
    this();
    this.user    = user;
    this.post    = post;
    this.content = content;
  }

  public Comment() {
    time = LocalDateTime.now();
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
