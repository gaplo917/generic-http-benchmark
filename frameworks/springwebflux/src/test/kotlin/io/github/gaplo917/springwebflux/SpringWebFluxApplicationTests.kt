package io.github.gaplo917.springwebflux

import io.github.gaplo917.springwebflux.services.IOService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebFlux
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient


@ExtendWith(SpringExtension::class)
@WebFluxTest
@AutoConfigureWebFlux
@Import(IOService::class)
class SpringWebFluxApplicationTests {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun endpointsShouldReady() {
        listOf(
            "/webflux-bio/0",
            "/webflux-bio-reactor-in-vt/0",
            "/webflux-bio-structured-concurrency-parallel/0",
            "/webflux-bio-coroutine-in-vt/0",
            "/webflux-bio-coroutine-in-vt-parallel/0",
            "/webflux-nio-reactor/0",
            "/webflux-nio-reactor-parallel/0",
            "/webflux-nio-reactor-structured-concurrency/0",
            "/webflux-nio-coroutine/0",
            "/webflux-nio-coroutine-parallel/0",
        ).forEach {
            webTestClient
                .get().uri(it)
                .exchange()
                .expectStatus().isOk
        }
    }
}
