package dev.frilly.locket.controller;

import dev.frilly.locket.data.FriendRequest;
import dev.frilly.locket.data.Friendship;
import dev.frilly.locket.data.User;
import dev.frilly.locket.dto.req.UsernameRequest;
import dev.frilly.locket.dto.res.PaginatedResponse;
import dev.frilly.locket.dto.res.UserResponse;
import dev.frilly.locket.repo.FriendRequestRepository;
import dev.frilly.locket.repo.FriendshipRepository;
import dev.frilly.locket.repo.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for handling friend requests.
 */
@RestController
public final class RequestsController {

  @Autowired
  private FriendRequestRepository frRepo;

  @Autowired
  private UserRepository userRepo;

  @Autowired
  private FriendshipRepository fsRepo;

  /**
   * GET /requests.
   * <p>
   * Retrieves a list of my friend requests.
   */
  @GetMapping("/requests")
  public PaginatedResponse<UserResponse> getRequests(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "per_page", defaultValue = "20") int perPage
  ) {
    final var auth = SecurityContextHolder.getContext().getAuthentication();
    final var user = (User) auth.getPrincipal();

    final var query = frRepo.findAllByReceiver(user,
        PageRequest.of(page, perPage, Sort.by("user1")));

    return new PaginatedResponse<>(query.getTotalElements(),
        query.getTotalPages(), page, perPage,
        query.get().map(fr -> fr.sender().makeResponse()).toList());
  }

  /**
   * POST /requests.
   * Sends a new request.
   */
  @PostMapping("/requests")
  public UserResponse postRequests(@RequestBody final UsernameRequest body) {
    final var auth   = SecurityContextHolder.getContext().getAuthentication();
    final var user   = (User) auth.getPrincipal();
    final var target = userRepo.findByUsername(body.username());

    if (target.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    if (user.id() == target.get().id()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    if (frRepo.findBySenderAndReceiver(user, target.get()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT);
    }

    final var req = frRepo.findBySenderAndReceiver(target.get(), user);
    if (req.isPresent()) {
      frRepo.delete(req.get());

      final Friendship fs;
      if (user.id() < target.get().id()) {
        fs = new Friendship(user, target.get());
      } else {
        fs = new Friendship(target.get(), user);
      }

      fsRepo.save(fs);
      throw new ResponseStatusException(HttpStatus.NO_CONTENT);
    }

    final var fr = new FriendRequest(user, target.get());
    frRepo.save(fr);
    return target.get().makeResponse();
  }

  /**
   * DELETE /requests
   * Delete a friend request sent.
   */
  @DeleteMapping("/requests")
  public UserResponse deleteRequest(@Valid @RequestBody final UsernameRequest body) {
    final var auth   = SecurityContextHolder.getContext().getAuthentication();
    final var user   = (User) auth.getPrincipal();
    final var target = userRepo.findByUsername(body.username());

    if (target.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    final var fr = frRepo.findBySenderAndReceiver(user, target.get());

    if (fr.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NO_CONTENT);
    }

    frRepo.delete(fr.get());
    return target.get().makeResponse();
  }

}
