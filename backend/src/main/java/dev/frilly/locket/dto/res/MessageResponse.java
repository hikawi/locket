package dev.frilly.locket.dto.res;

import dev.frilly.locket.data.Message;

import java.time.LocalDateTime;

/**
 * Represents a recorded message.
 *
 * @param id       the message id
 * @param sender   the sender response
 * @param receiver the receiver response
 * @param content  the content of the message
 * @param time     the time when the message is logged
 * @param state    the message's state
 */
public record MessageResponse(long id, UserResponse sender,
                              UserResponse receiver, String content,
                              LocalDateTime time, Message.State state) {

}
