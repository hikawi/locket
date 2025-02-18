package dev.frilly.locket.controller

import dev.frilly.locket.LocketBackendApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * The controller for handling on route ("/").
 */
@RestController
class IndexController {

    data class VersionResponse(val version: String)

    /**
     * Retrieves the API version of the backend.
     */
    @GetMapping("/")
    fun getIndex(): VersionResponse {
        return VersionResponse(LocketBackendApplication.API_VERSION)
    }

}