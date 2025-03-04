package dev.frilly.locket.data;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity model for a friend request table.
 */
@Entity
@Table(
    name = "friend_requests", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sender_id", "receiver_id"})
}
)
public final class FriendRequest {

  @Column(nullable = false)
  private final LocalDateTime time = LocalDateTime.now();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", nullable = false)
  private final User sender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id", nullable = false)
  private final User receiver;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  public FriendRequest(User sender, User receiver) {
    this.sender   = sender;
    this.receiver = receiver;
  }

  public long id() {
    return id;
  }

  public LocalDateTime time() {
    return time;
  }

  public User sender() {
    return sender;
  }

  public User receiver() {
    return receiver;
  }

}
