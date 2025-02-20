package dev.frilly.locket.controller

import com.cloudinary.Cloudinary
import dev.frilly.locket.controller.dto.AbstractPost
import dev.frilly.locket.controller.dto.AbstractUser
import dev.frilly.locket.controller.dto.DeletePostsRequest
import dev.frilly.locket.controller.dto.GetPostsResponse
import dev.frilly.locket.data.Post
import dev.frilly.locket.data.User
import dev.frilly.locket.repo.PostsRepository
import dev.frilly.locket.repo.UserRepository
import jakarta.validation.Valid
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

    private fun uploadImage(image: MultipartFile): Pair<HttpStatus, String> {
        val mimeType = image.contentType ?: ""

        if (mimeType !in setOf("image/png", "image/jpeg", "image/webp")) {
            return HttpStatus.BAD_REQUEST to ""
        }

        // Save to a temp file
        if (image.size > tenMegabytes) {
            return HttpStatus.PAYLOAD_TOO_LARGE to ""
        }

        val url = cloudinary.uploader().upload(
            image.bytes, emptyMap<String, String>(),
        ).get("url") as String
        return HttpStatus.OK to url
    }

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
        val (uploadResult, url) = uploadImage(image)

        if (uploadResult != HttpStatus.OK) {
            return ResponseEntity.status(uploadResult).build()
        }

        val post = Post(
            id = 0,
            user = user,
            viewers = userRepository
                .findByUsernameIn(viewersSet)
                .toMutableSet(),
            imageLink = url,
            message = message,
        )
        postRepository.save(post)

        return ResponseEntity.created(URI(url)).build()
    }

    /**
     * Edits a post, given a post ID.
     */
    @PutMapping("/posts", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun putPosts(
        @RequestPart("id") id: Long,
        @RequestPart("image") image: MultipartFile? = null,
        @RequestPart("message") message: String? = null,
        @RequestPart("viewers") viewers: String? = null,
    ): ResponseEntity<AbstractPost> {
        val auth = SecurityContextHolder.getContext().authentication
        val user = auth.principal as User
        val post = postRepository.findById(id).getOrNull()
            ?: return ResponseEntity.notFound().build()

        if (post.user != user) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        if (image != null) {
            val (result, url) = uploadImage(image)
            if (result != HttpStatus.OK) {
                return ResponseEntity.status(result).build()
            }

            post.imageLink = url
        }

        if (message != null) {
            post.message = message
        }

        if (viewers != null) {
            val viewersSet = viewers.split(",").toSet()
            post.viewers.clear()
            post.viewers.addAll(
                userRepository.findByUsernameIn(viewersSet).toSet()
            )
        }

        postRepository.save(post)
        return ResponseEntity.ok(
            AbstractPost(
                id = post.id,
                poster = AbstractUser(user.username, user.avatarUrl),
                image = post.imageLink,
                message = post.message,
                time = post.time,
            ),
        )
    }

    /**
     * Deletes a post, given a post ID.
     */
    @DeleteMapping("/posts")
    fun deletePosts(@Valid @RequestBody body: DeletePostsRequest)
            : ResponseEntity<AbstractPost> {
        val auth = SecurityContextHolder.getContext().authentication
        val user = auth.principal as User
        val post = postRepository.findById(body.id).getOrNull()
            ?: return ResponseEntity.notFound().build()

        if (post.user != user) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        postRepository.delete(post)
        return ResponseEntity.ok(
            AbstractPost(
                id = post.id,
                poster = AbstractUser(user.username, user.avatarUrl),
                image = post.imageLink,
                message = post.message,
                time = post.time,
            ),
        )
    }

}
