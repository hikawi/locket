package dev.frilly.locket.data;

import dev.frilly.locket.data.key.FriendshipKey;
import jakarta.persistence.*;

/**
 * Entity model class for a friendship between two users.
 */
@Entity
@Table(
    name = "friendships",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user1", "user2"})}
)
public final class Friendship {

  @ManyToOne
  @MapsId("user1")
  @JoinColumn(name = "user1", nullable = false)
  private User user1;

  @ManyToOne
  @MapsId("user2")
  @JoinColumn(name = "user2", nullable = false)
  private User user2;

  @EmbeddedId
  private FriendshipKey key;

  public Friendship() {
  }

  public Friendship(User user1, User user2) {
    this.user1 = user1;
    this.user2 = user2;
  }

  public User user1() {
    return user1;
  }

  public User user2() {
    return user2;
  }

}
