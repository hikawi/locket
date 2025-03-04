package dev.frilly.locket.controller;

import dev.frilly.locket.data.User;
import dev.frilly.locket.dto.req.ProfileRequest;
import dev.frilly.locket.dto.res.UserResponse;
import dev.frilly.locket.repo.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for fetching profiles.
 */
@RestController
public final class ProfilesController {

  @Autowired
  private UserRepository userRepo;

  @Autowired
  private BCryptPasswordEncoder encoder;

  /**
   * Handles GET /profiles. This route is unauthenticated.
   */
  @GetMapping("/profiles")
  public UserResponse getProfile(
      @RequestParam(
          defaultValue = ""
      ) String username
  ) {
    final var auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth.getPrincipal() instanceof User user && username.isBlank()) {
      return user.makeResponse();
    } else {
      final var target = userRepo.findByUsername(username);
      if (target.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
      }

      return target.get().makeResponse();
    }
  }

  /**
   * Controller for PUT /profiles.
   */
  @PutMapping("/profiles")
  public UserResponse putProfile(@Valid @RequestBody final ProfileRequest body) {
    final var auth = SecurityContextHolder.getContext().getAuthentication();
    final var user = (User) auth.getPrincipal();

    if (body.username() != null) {
      final var neighbor = userRepo.findByUsername(body.username());
      if (neighbor.isPresent() && neighbor.get().id() != user.id()) {
        throw new ResponseStatusException(HttpStatus.CONFLICT);
      }

      user.setUsername(body.username());
    }

    if (body.password() != null) {
      user.setPassword(encoder.encode(body.password()));
    }

    if (body.email() != null) {
      final var neighbor = userRepo.findByEmail(body.email());
      if (neighbor.isPresent() && neighbor.get().id() != user.id()) {
        throw new ResponseStatusException(HttpStatus.CONFLICT);
      }

      user.setEmail(body.email());
    }

    if (body.birthdate() != null) {
      user.setBirthdate(body.birthdate());
    }

    final var saved = userRepo.save(user);
    return saved.makeResponse();
  }

}
