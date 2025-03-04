package dev.frilly.locket.controller;

import dev.frilly.locket.Versioning;
import dev.frilly.locket.dto.res.VersionResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The controller for handling on route ("/").
 */
@RestController
public final class IndexController {

  /**
   * GET /: Retrieves the backend's version.
   */
  @GetMapping("/")
  public VersionResponse getIndex() {
    return new VersionResponse(Versioning.getCurrent().toString());
  }

}
