package dev.frilly.locket.dto.req;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

/**
 * Dto for a request containing messages data.
 */
public class MessageRequest {

  @Positive
  private final long receiver;

  @NotEmpty
  private final String content;

  public MessageRequest(long receiver, String content) {
    this.receiver = receiver;
    this.content  = content;
  }

  public long receiver() {
    return receiver;
  }

  public String content() {
    return content;
  }

}
