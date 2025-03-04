package dev.frilly.locket.controller;

import dev.frilly.locket.data.User;
import dev.frilly.locket.dto.req.IdRequest;
import dev.frilly.locket.dto.res.PaginatedResponse;
import dev.frilly.locket.dto.res.UserResponse;
import dev.frilly.locket.repo.FriendshipRepository;
import dev.frilly.locket.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for handling friends.
 */
@RestController
public final class FriendsController {

  @Autowired
  private FriendshipRepository friendshipRepo;

  @Autowired
  private UserRepository userRepo;

  /**
   * GET /friends.
   * <p>
   * Retrieves a list of my friends, for example who can I share with.
   */
  @GetMapping("/friends")
  public PaginatedResponse<UserResponse> getFriends(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "per_page", defaultValue = "20") int perPage
  ) {
    final var auth = SecurityContextHolder.getContext().getAuthentication();
    final var user = (User) auth.getPrincipal();

    final var query = friendshipRepo.findByUser(user,
        PageRequest.of(page, perPage, Sort.by("user1")));

    return new PaginatedResponse<>(query.getTotalElements(),
        query.getTotalPages(), page, perPage, query.get().map(fs -> {
      final var other = fs.user1().id() == user.id() ? fs.user2() : fs.user1();
      return other.makeResponse();
    }).toList());
  }

  /**
   * DELETE /friends.
   * <p>
   * Remove a friend.
   */
  @DeleteMapping("/friends")
  public UserResponse deleteFriends(@RequestBody final IdRequest body) {
    final var auth   = SecurityContextHolder.getContext().getAuthentication();
    final var user   = (User) auth.getPrincipal();
    final var target = userRepo.findById(body.id());

    if (target.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    final var fs = friendshipRepo.findWithTwoUsers(user, target.get());
    if (fs.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NO_CONTENT);
    }

    friendshipRepo.delete(fs.get());
    return target.get().makeResponse();
  }

}
