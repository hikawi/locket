package dev.frilly.locket.repo;

import dev.frilly.locket.data.Friendship;
import dev.frilly.locket.data.User;
import dev.frilly.locket.data.key.FriendshipKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * CRUD repository for friendships
 */
public interface FriendshipRepository
    extends JpaRepository<Friendship, FriendshipKey> {

  @Query(
      "select f from Friendship f where f.user1 = :user or f.user2 = :user"
  )
  Page<Friendship> findByUser(final User user, final Pageable pageable);

  @Query(
      "select f from Friendship f where (f.user1 = :user1 and f.user2 = " +
      ":user2) or (f.user2 = :user1 and f.user1 = :user2)"
  )
  Optional<Friendship> findWithTwoUsers(final User user1, final User user2);

}
