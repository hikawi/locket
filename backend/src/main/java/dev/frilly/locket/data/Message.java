package dev.frilly.locket.data;

import dev.frilly.locket.dto.res.MessageResponse;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Represents a Message object to send between two users.
 */
@Entity
@Table(name = "messages")
public final class Message {

  @Column(nullable = false)
  private final LocalDateTime time;

  @ManyToOne
  @JoinColumn(name = "sender_id", nullable = false)
  private User sender;

  @ManyToOne
  @JoinColumn(name = "receiver_id", nullable = false)
  private User receiver;

  @Column(nullable = false, columnDefinition = "text")
  private String content;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private State state;

  public Message(User sender, User receiver, String content) {
    this();
    this.sender   = sender;
    this.receiver = receiver;
    this.content  = content;
  }

  public Message() {
    state = State.SENT;
    time  = LocalDateTime.now();
  }

  public Message setState(State state) {
    this.state = state;
    return this;
  }

  public long id() {
    return id;
  }

  public User sender() {
    return sender;
  }

  public User receiver() {
    return receiver;
  }

  public String content() {
    return content;
  }

  public LocalDateTime time() {
    return time;
  }

  public State state() {
    return state;
  }

  public MessageResponse makeResponse() {
    return new MessageResponse(id, sender.makeResponse(),
        receiver.makeResponse(), content, time, state);
  }

  /**
   * Represents the state of the message.
   */
  public enum State {

    /**
     * This is when the message is received correctly by the server, and
     * logged in the database, but the receiver has not sent an
     * acknowledgement to the server that it has received the message.
     * <p>
     * For example, if the receiver does not have Internet.
     */
    SENT,

    /**
     * This is updated when the receiver sends an acknowledge request to the
     * server, telling that the message has been received on the other end.
     */
    DELIVERED,

    /**
     * This is updated when the receiver clicks on the message tab and reads
     * the message entirely.
     */
    READ,

  }

}
