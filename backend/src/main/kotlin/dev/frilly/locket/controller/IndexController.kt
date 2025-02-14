package dev.frilly.locket.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IndexController {

    data class TestReturnObject(val message: String)

    @GetMapping("/")
    fun getIndex(): TestReturnObject {
        return TestReturnObject("Hello, World")
    }

    @GetMapping("/test")
    fun getTest(): TestReturnObject {
        return TestReturnObject("Authenticated route")
    }

}