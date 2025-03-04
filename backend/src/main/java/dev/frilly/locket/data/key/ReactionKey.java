package dev.frilly.locket.data.key;

import jakarta.persistence.Embeddable;

/**
 * Composite primary key for {@link dev.frilly.locket.data.Reaction}.
 */
@Embeddable
public record ReactionKey(long postId, long userId) {

}
