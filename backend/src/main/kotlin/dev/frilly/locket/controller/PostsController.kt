package dev.frilly.locket.controller

import com.cloudinary.Cloudinary
import dev.frilly.locket.controller.dto.AbstractPost
import dev.frilly.locket.controller.dto.AbstractUser
import dev.frilly.locket.controller.dto.GetPostsResponse
import dev.frilly.locket.data.Post
import dev.frilly.locket.data.User
import dev.frilly.locket.repo.PostsRepository
import dev.frilly.locket.repo.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

/**
 * Controller for handling route /posts. Concerned with receiving multi-part
 * form data and upload images correctly on an image-hosting service.
 */
@RestController
class PostsController {

    private val tenMegabytes = 10 * 1024 * 1024

    @Autowired
    private lateinit var postRepository: PostsRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var cloudinary: Cloudinary

    /**
     * Retrieves a list of posts within a timeframe.
     */
    @GetMapping("/posts")
    fun getPosts(
        @RequestParam("from") from: LocalDateTime,
        @RequestParam("to") to: LocalDateTime = LocalDateTime.now(),
    ): GetPostsResponse {
        val auth = SecurityContextHolder.getContext().authentication
        val user = auth.principal as User

        val result = postRepository.getPostsInRange(user, from, to)
        return GetPostsResponse(
            total = result.size,
            results = result.map {
                AbstractPost(
                    id = it.id,
                    poster = AbstractUser(
                        it.user.username,
                        avatar = user.avatarUrl,
                    ),
                    image = it.imageLink,
                    message = it.message,
                    time = it.time,
                )
            }
        )
    }

    /**
     * Uploads a new post to friends.
     */
    @PostMapping("/posts", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postPosts(
        @RequestPart("image") image: MultipartFile,
        @RequestPart("viewers") viewers: String = "",
        @RequestPart("message") message: String = "",
    ): ResponseEntity<Unit> {
        val auth = SecurityContextHolder.getContext().authentication
        val user = auth.principal as User

        val viewersSet = viewers.split(",").map { it.lowercase() }.toSet()
        val mimeType = image.contentType

        if (mimeType !in setOf("image/png", "image/jpeg", "image/webp")) {
            return ResponseEntity.badRequest().build()
        }

        // Save to a temp file
        if (image.size > tenMegabytes) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build()
        }

        val url = cloudinary.uploader().upload(
            image.bytes, emptyMap<String, String>(),
        ).get("url") as String

        val post = Post(
            id = 0,
            user = user,
            viewers = viewersSet.map {
                userRepository.findByUsername(it).getOrNull()
            }.filterNotNull().toSet(),
            imageLink = url,
            message = message,
        )
        postRepository.save(post)

        return ResponseEntity.created(URI(url)).build()
    }

    /**
     * Edits a post, given a post ID.
     */
    @PutMapping("/posts")
    fun putPosts() {

    }

}
