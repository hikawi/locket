package dev.frilly.locket.controller;

import com.cloudinary.Cloudinary;
import dev.frilly.locket.data.User;
import dev.frilly.locket.dto.req.ProfileRequest;
import dev.frilly.locket.dto.res.UserResponse;
import dev.frilly.locket.repo.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controller for fetching profiles.
 */
@RestController
public final class ProfilesController {

  @Autowired
  private UserRepository userRepo;

  @Autowired
  private BCryptPasswordEncoder encoder;

  @Autowired
  private Cloudinary cloudinary;

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
   * Controller for PUTTING a new avatar.
   */
  @PostMapping(
      value = "/profiles/avatar",
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
  )
  public UserResponse putProfileAvatar(@RequestPart final MultipartFile image) {
    final var auth = SecurityContextHolder.getContext().getAuthentication();
    final var user = (User) auth.getPrincipal();

    if (image.getSize() > 10 * 1024 * 1024) {
      throw new ResponseStatusException(HttpStatus.REQUEST_ENTITY_TOO_LARGE);
    }

    final var allowed = List.of("image/webp", "image/jpeg", "image/png");
    if (!allowed.contains(image.getContentType())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    try {
      final var res = cloudinary.uploader().upload(image.getBytes(), Map.of());
      user.setAvatarUrl((String) res.get("secure_url"));
      return userRepo.save(user).makeResponse();
    } catch (IOException ex) {
      ex.printStackTrace();
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
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
