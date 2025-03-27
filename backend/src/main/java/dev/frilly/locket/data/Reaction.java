package dev.frilly.locket.data;

import dev.frilly.locket.data.key.ReactionKey;
import jakarta.persistence.*;

/**
 * Represents a reaction to a post. Should this be separate from [[Comment]]?
 */
@Entity
@Table(name = "reactions")
public final class Reaction {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private final ReactionType reaction;

  @EmbeddedId
  private ReactionKey id;

  @ManyToOne(cascade = {CascadeType.ALL})
  @MapsId("postId")
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @ManyToOne(cascade = {CascadeType.ALL})
  @MapsId("userId")
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  public Reaction(Post post, User user) {
    this();
    this.id   = new ReactionKey(post.id(), user.id());
    this.post = post;
    this.user = user;
  }

  public Reaction() {
    reaction = ReactionType.LIKE;
  }

  public ReactionKey id() {
    return id;
  }

  public Post post() {
    return post;
  }

  public User user() {
    return user;
  }

  public ReactionType reaction() {
    return reaction;
  }

  /**
   * Represents a type of reaction a user can react to another user's Locket
   * post.
   */
  public enum ReactionType {

    LIKE, LOVE, FUNNY, SAD, ANGRY,

  }

}
