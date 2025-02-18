package dev.frilly.locket

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["dev.frilly.locket"])
class LocketBackendApplication {

    companion object {

        /** The API VERSION of the application.
         */
        @JvmStatic
        val API_VERSION = "0.2"

    }

}

fun main(args: Array<String>) {
    runApplication<LocketBackendApplication>(*args)
}
