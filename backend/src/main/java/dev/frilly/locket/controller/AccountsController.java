package dev.frilly.locket.controller;

import dev.frilly.locket.data.User;
import dev.frilly.locket.dto.req.CredentialsRequest;
import dev.frilly.locket.dto.req.RegisterRequest;
import dev.frilly.locket.dto.res.TokenResponse;
import dev.frilly.locket.repo.UserRepository;
import dev.frilly.locket.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for accounts-related routes.
 */
@RestController
public final class AccountsController {

  @Autowired
  private JwtService jwtService;

  @Autowired
  private BCryptPasswordEncoder encoder;

  @Autowired
  private UserRepository userRepo;

  /**
   * Handles the POST login request.
   */
  @PostMapping("/login")
  public TokenResponse postLogin(@Valid @RequestBody final CredentialsRequest body) {
    final var user = userRepo.findByUsername(body.username());

    // No user exists.
    if (user.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    // Wrong password
    if (!encoder.matches(body.password(), user.get().password())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    return new TokenResponse(jwtService.encode(user.get().id()));
  }

  /**
   * Handles the POST register request.
   */
  @PostMapping("/register")
  public TokenResponse postRegister(@Valid @RequestBody final RegisterRequest body) {
    if (userRepo.findByUsernameAndEmail(body.username(), body.email())
        .isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT);
    }

    final var user = userRepo.save(new User().setEmail(body.email())
        .setUsername(body.username())
        .setPassword(encoder.encode(body.password())));
    return new TokenResponse(jwtService.encode(user.id()));
  }

}
