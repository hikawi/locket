package dev.frilly.locket.data.key;

import jakarta.persistence.Embeddable;

/**
 * Composite key for {@link dev.frilly.locket.data.Friendship} model.
 */
@Embeddable
public final class FriendshipKey {

  private final long user1;
  private final long user2;

  /**
   * Constructs a new friendship key.
   *
   * @param user1 the first user
   * @param user2 the second user
   */
  public FriendshipKey(final long user1, final long user2) {
    if (user1 > user2) {
      this.user1 = user2;
      this.user2 = user1;
    } else {
      this.user1 = user1;
      this.user2 = user2;
    }
  }

  public long user1() {
    return user1;
  }

  public long user2() {
    return user2;
  }

}
