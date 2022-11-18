package io.github.gaplo917.springmvc

import io.github.gaplo917.springmvc.data.DummyResponse
import kotlinx.coroutines.future.await
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

@Controller
class NonBlockingIOController {
    private val scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    private fun nonBlockingIO(ioDelay: Long): CompletableFuture<DummyResponse> {
        return CompletableFuture.supplyAsync(
            { DummyResponse.dummy() },
            CompletableFuture.delayedExecutor(ioDelay, TimeUnit.MILLISECONDS, scheduledExecutorService)
        )
    }

    private fun dependentNonBlockingIO(ioDelay: Long, resp: DummyResponse): CompletableFuture<List<DummyResponse>> {
        return CompletableFuture.supplyAsync(
            { listOf(resp, DummyResponse.dummy()) },
            CompletableFuture.delayedExecutor(ioDelay, TimeUnit.MILLISECONDS, scheduledExecutorService)
        )
    }

    @RequestMapping("/mvc-non-blocking-future/{ioDelay}")
    fun nonBlockingFutureApi(@PathVariable ioDelay: Long): Future<ResponseEntity<List<DummyResponse>>> {
        return nonBlockingIO(ioDelay)
            .thenCompose { resp -> dependentNonBlockingIO(ioDelay, resp) }
            .thenApply { result -> ResponseEntity.ok(result) }
    }

    @RequestMapping("/mvc-non-blocking-coroutine/{ioDelay}")
    suspend fun nonBlockingCoroutineApi(@PathVariable ioDelay: Long): ResponseEntity<List<DummyResponse>> {
        // kotlinx-coroutines-jdk8 extension
        // `suspend fun <T> CompletionStage<T>.await(): T`
        val resp = nonBlockingIO(ioDelay).await()
        val result = dependentNonBlockingIO(ioDelay, resp).await()
        return ResponseEntity.ok(result)
    }

}
