package dev.frilly.locket

import com.cloudinary.Cloudinary
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["dev.frilly.locket"])
class LocketBackendApplication {

    @Value("\${secrets.cloudinary}")
    private lateinit var cloudinaryUrl: String

    companion object {

        /** The API VERSION of the application.
         */
        @JvmStatic
        val API_VERSION = "0.3"

    }

    /**
     * The Cloudinary Bean.
     */
    @Bean
    fun cloudinary(): Cloudinary {
        return Cloudinary(cloudinaryUrl)
    }

}

fun main(args: Array<String>) {
    runApplication<LocketBackendApplication>(*args)
}
