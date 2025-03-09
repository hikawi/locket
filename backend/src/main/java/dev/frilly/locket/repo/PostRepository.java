package dev.frilly.locket.repo;

import dev.frilly.locket.data.Post;
import dev.frilly.locket.data.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

/**
 * A CRUD Repository for managing posts.
 */
public interface PostRepository extends JpaRepository<Post, Long> {

  /**
   * Retrieves a list of posts within a certain timeframe.
   *
   * @param viewer the viewer
   * @param from   the time to start
   * @param to     the time to end
   *
   * @return the list
   */
  @Query(
      "select p from Post p where p.time >= :from and p.time <= :to and " +
      "(:viewer member of p.viewers or p.user = :viewer)"
  )
  Page<Post> getPostsInRange(
      final User viewer,
      final LocalDateTime from,
      final LocalDateTime to,
      final Pageable pageable
  );

}
