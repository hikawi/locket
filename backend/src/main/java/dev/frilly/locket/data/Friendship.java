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

  @EmbeddedId
  private final FriendshipKey key;
  @ManyToOne
  @MapsId("user1")
  @JoinColumn(name = "user1", nullable = false)
  private User user1;
  @ManyToOne
  @MapsId("user2")
  @JoinColumn(name = "user2", nullable = false)
  private User user2;

  public Friendship() {
    key = new FriendshipKey();
  }

  public Friendship(User user1, User user2) {
    key        = new FriendshipKey(user1.id(), user2.id());
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
