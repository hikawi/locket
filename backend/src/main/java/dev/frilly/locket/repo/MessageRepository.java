package dev.frilly.locket.repo;

import dev.frilly.locket.data.Message;
import dev.frilly.locket.data.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

/**
 * CRUD repository for messages.
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

  @Query(
      "select m from Message m where m.receiver = :receiver and m.time >= " + "from and m.time <= until"
  )
  Page<Message> findMessages(
      final User receiver,
      final LocalDateTime from,
      final LocalDateTime until,
      final Pageable pageable
  );

}
