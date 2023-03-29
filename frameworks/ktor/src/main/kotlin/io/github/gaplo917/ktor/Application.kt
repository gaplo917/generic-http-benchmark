package io.github.gaplo917.ktor

import io.github.gaplo917.ktor.plugins.configureMonitoring
import io.github.gaplo917.ktor.plugins.configureRouting
import io.github.gaplo917.ktor.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureMonitoring()
    configureSerialization()
    configureRouting()
}
