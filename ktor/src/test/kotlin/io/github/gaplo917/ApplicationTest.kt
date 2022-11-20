package io.github.gaplo917

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.micrometer.prometheus.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlin.test.*
import io.ktor.server.testing.*
import io.github.gaplo917.ktor.plugins.*

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureMonitoring()
            configureSerialization()
            configureRouting()
        }
        client.get("ktor-nio/0").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}