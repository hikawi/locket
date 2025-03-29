package dev.frilly.locket.repo;

import dev.frilly.locket.data.FriendRequest;
import dev.frilly.locket.data.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * CRUD Repository for handling friend requests.
 */
public interface FriendRequestRepository
    extends JpaRepository<FriendRequest, Long> {

  /**
   * Gets all friend requests from the sender's POV.
   *
   * @param sender   the sender
   * @param pageable paging object
   *
   * @return a page of friend requests
   */
  Page<FriendRequest> findAllBySender(
      final User sender,
      final Pageable pageable
  );

  /**
   * Gets all friend requests from the receiver's POV.
   *
   * @return the list of requests
   */
  Page<FriendRequest> findAllByReceiver(
      final User receiver,
      final Pageable pageable
  );

  /**
   * Finds a friend request relationship between a sender and a receiver.
   *
   * @return the friend request
   */
  @Query(
      "select r from FriendRequest r where r.sender = :sender and r" +
      ".receiver = :receiver"
  )
  Optional<FriendRequest> findBySenderAndReceiver(
      final User sender,
      final User receiver
  );

}
