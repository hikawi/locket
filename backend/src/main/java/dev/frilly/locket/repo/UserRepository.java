package dev.frilly.locket.repo;

import dev.frilly.locket.data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A CRUD repository for managing User objects.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Finds a user based on the username, after lowercasing both.
   *
   * @param username the username to match
   *
   * @return the user object, wrapped
   */
  @Query("select u from User u where lower(u.username) = lower(:username)")
  Optional<User> findByUsername(final String username);

  /**
   * Finds a user based on the email, after lowercasing both.
   *
   * @param email the email to match
   *
   * @return the user object, wrapped
   */
  @Query("select u from User u where lower(u.email) = lower(:email)")
  Optional<User> findByEmail(final String email);

  /**
   * Finds a user based on the username and email at the same time.
   *
   * @param username the username
   * @param email    the email
   *
   * @return the user object, wrapped
   */
  @Query(
      "select u from User u where lower(u.email) = lower(:email) or lower(u" + ".username) = lower(:username)"
  )
  Optional<User> findByUsernameAndEmail(
      final String username,
      final String email
  );

  /**
   * Attempts to match users within the list of usernames.
   *
   * @param usernames the usernames
   *
   * @return the list of users
   */
  @Query("select u from User u where lower(u.username) in :usernames")
  List<User> findByUsernameIn(final Set<String> usernames);

}
