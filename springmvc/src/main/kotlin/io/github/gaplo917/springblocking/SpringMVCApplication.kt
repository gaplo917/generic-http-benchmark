package io.github.gaplo917.springblocking

import io.github.gaplo917.springblocking.data.DummyResponse
import jdk.incubator.concurrent.StructuredTaskScope
import kotlinx.coroutines.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.util.concurrent.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@SpringBootApplication
class SpringMVCApplication

@Controller
class BenchmarkController {
    private val scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor()
    private val virtualThreadCoroutineDispatcher = virtualThreadExecutor.asCoroutineDispatcher()

    private fun blockingIO(ioDelay: Long): DummyResponse {
        Thread.sleep(ioDelay)
        return DummyResponse.dummy()
    }

    private fun nonBlockingIO(ioDelay: Long): CompletableFuture<DummyResponse> {
        return CompletableFuture.supplyAsync(
            { DummyResponse.dummy() },
            CompletableFuture.delayedExecutor(ioDelay, TimeUnit.MILLISECONDS, scheduledExecutorService)
        )
    }

    private suspend fun nonBlockingIOCoroutine(ioDelay: Long): DummyResponse = suspendCoroutine { cnt ->
        nonBlockingIO(ioDelay).thenAccept {
            cnt.resume(it)
        }
    }

    fun blockingVirtualThread(ioDelay: Long): DummyResponse {
        return StructuredTaskScope.ShutdownOnFailure().use { scope ->
            val future = scope.fork { blockingIO(ioDelay) }
            scope.join()
            scope.throwIfFailed()
            future.resultNow()
        }
    }

    @RequestMapping("/blocking/{ioDelay}")
    fun blockingApi(@PathVariable ioDelay: Long): ResponseEntity<DummyResponse> {
        return ResponseEntity.ok(blockingIO(ioDelay))
    }

    @RequestMapping("/blocking-coroutine/{ioDelay}")
    suspend fun blockingCoroutineApi(@PathVariable ioDelay: Long): ResponseEntity<DummyResponse> {
        return ResponseEntity.ok(blockingIO(ioDelay))
    }

    @RequestMapping("/blocking-virtual-thread/{ioDelay}")
    fun blockingNativeVirtualThreadApi(@PathVariable ioDelay: Long): ResponseEntity<DummyResponse> {
        val resp = StructuredTaskScope.ShutdownOnFailure().use { scope ->
            val future = scope.fork { blockingIO(ioDelay) }
            scope.join()
            scope.throwIfFailed()
            future.resultNow()
        }
        return ResponseEntity.ok(resp)
    }


    @RequestMapping("/non-blocking/{ioDelay}")
    fun nonBlockingFutureApi(@PathVariable ioDelay: Long): Future<ResponseEntity<DummyResponse>> {
        return nonBlockingIO(ioDelay).thenApplyAsync {
            ResponseEntity.ok(it)
        }
    }

    @RequestMapping("/non-blocking-coroutine/{ioDelay}")
    suspend fun nonBlockingCoroutineApi(@PathVariable ioDelay: Long): ResponseEntity<DummyResponse> {
        return ResponseEntity.ok(nonBlockingIOCoroutine(ioDelay))
    }

    @RequestMapping("/blocking-vt-future/{ioDelay}")
    fun blockingFutureInVTApi(@PathVariable ioDelay: Long): Future<ResponseEntity<DummyResponse>> {
        return CompletableFuture.supplyAsync(
            { blockingIO(ioDelay) },
            virtualThreadExecutor
        ).thenApply { ResponseEntity.ok(it) }
    }

    @RequestMapping("/blocking-vt-coroutine/{ioDelay}")
    suspend fun blockingSuspendInVTApi(@PathVariable ioDelay: Long): ResponseEntity<DummyResponse> =
        withContext(virtualThreadCoroutineDispatcher) {
            ResponseEntity.ok(blockingIO(ioDelay))
        }

}

fun main(args: Array<String>) {
    runApplication<SpringMVCApplication>(*args)
}
