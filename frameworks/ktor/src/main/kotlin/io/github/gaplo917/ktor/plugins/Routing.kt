package io.github.gaplo917.ktor.plugins

import io.github.gaplo917.ktor.data.DummyResponse
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun Application.configureRouting() {
    val scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    suspend fun nonBlockingIO(ioDelay: Long): DummyResponse = suspendCoroutine { cnt ->
        scheduledExecutorService.schedule(
            { cnt.resume(DummyResponse.dummy()) },
            ioDelay,
            TimeUnit.MILLISECONDS
        )
    }

    suspend fun dependentNonBlockingIO(ioDelay: Long, resp: DummyResponse): List<DummyResponse> =
        suspendCoroutine { cnt ->
            scheduledExecutorService.schedule(
                { cnt.resume(listOf(resp, DummyResponse.dummy())) },
                ioDelay,
                TimeUnit.MILLISECONDS
            )
        }

    routing {
        get("/ktor-nio/{ioDelay}") {
            val ioDelay = call.parameters["ioDelay"]?.toLong()!!
            val resp = nonBlockingIO(ioDelay)
            val result = dependentNonBlockingIO(ioDelay, resp)
            call.respond(result)
        }
    }
}
