package dev.frilly.locket.controller;

import dev.frilly.locket.data.DeviceToken;
import dev.frilly.locket.data.User;
import dev.frilly.locket.dto.req.CredentialsRequest;
import dev.frilly.locket.dto.req.RegisterRequest;
import dev.frilly.locket.dto.req.TokenRequest;
import dev.frilly.locket.dto.res.TokenResponse;
import dev.frilly.locket.repo.TokenRepository;
import dev.frilly.locket.repo.UserRepository;
import dev.frilly.locket.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
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

  @Autowired
  private TokenRepository tokenRepo;

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

  /**
   * Handles the POST /subscribe request.
   */
  @PostMapping("/subscribe")
  public TokenResponse postSubscribe(@Valid @RequestBody final TokenRequest body) {
    final var auth = SecurityContextHolder.getContext().getAuthentication();
    final var user = (User) auth.getPrincipal();

    final var token = tokenRepo.findByToken(body.token());
    if (token.isEmpty()) {
      final var obj = tokenRepo.save(
          new DeviceToken().setToken(body.token()).setUser(user));
      return new TokenResponse(obj.token());
    }

    token.get().setUser(user);
    tokenRepo.save(token.get());
    return new TokenResponse(body.token());
  }

  /**
   * Handles the DELETE /subscribe request.
   */
  @DeleteMapping("/subscribe")
  public TokenResponse deleteSubscribe(@Valid @RequestBody final TokenRequest body) {
    final var auth = SecurityContextHolder.getContext().getAuthentication();
    final var user = (User) auth.getPrincipal();

    final var token = tokenRepo.findByToken(body.token());
    if (token.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    if (token.get().user().id() != user.id()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    tokenRepo.delete(token.get());
    return new TokenResponse(body.token());
  }

}
