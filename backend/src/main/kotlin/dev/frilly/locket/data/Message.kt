package dev.frilly.locket.data

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Represents the state of the message.
 */
enum class MessageState {

    /**
     * This is when the message is received correctly by the server, and
     * logged in the database, but the receiver has not sent an
     * acknowledgement to the server that it has received the message.
     *
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

/**
 * Represents a direct message between two users.
 *
 * A table has:
 * - ID (Serial) primary key
 * - SENDER_ID (serial) references users
 * - RECEIVER_ID (serial) references users
 * - CONTENT (text)
 * - TIME (time)
 * - STATE (text) enum state, check [[MessageState]].
 */
@Entity
@Table(name = "messages")
data class Message(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: User,

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    val receiver: User,

    @Column(nullable = false, columnDefinition = "text")
    val content: String,

    @Column(nullable = false)
    val time: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val state: MessageState = MessageState.SENT,
)
