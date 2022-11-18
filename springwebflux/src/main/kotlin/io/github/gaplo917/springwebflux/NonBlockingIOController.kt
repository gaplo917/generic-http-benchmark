package io.github.gaplo917.springwebflux

import io.github.gaplo917.springwebflux.data.DummyResponse
import jdk.incubator.concurrent.StructuredTaskScope
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Controller
class NonBlockingIOController {
    private val scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val vtExecutor = Executors.newVirtualThreadPerTaskExecutor()

    fun nonBlockingIO(ioDelay: Long): Mono<DummyResponse> {
        return Mono.create { observer ->
            scheduledExecutorService.schedule(
                { observer.success(DummyResponse.dummy()) },
                ioDelay,
                TimeUnit.MILLISECONDS
            )
        }
    }

    private fun dependentNonBlockingIO(ioDelay: Long, resp: DummyResponse): Mono<List<DummyResponse>> {
        return Mono.create { observer ->
            scheduledExecutorService.schedule(
                { observer.success(listOf(resp, DummyResponse.dummy())) },
                ioDelay,
                TimeUnit.MILLISECONDS
            )
        }
    }

    @RequestMapping("/webflux-non-blocking-reactor/{ioDelay}")
    fun nonBlockingReactor(@PathVariable ioDelay: Long): Mono<ResponseEntity<List<DummyResponse>>> {
        return nonBlockingIO(ioDelay)
            .flatMap { resp1 -> dependentNonBlockingIO(ioDelay, resp1) }
            .map { result -> ResponseEntity.ok(result) }
    }

    @RequestMapping("/webflux-non-blocking-coroutine/{ioDelay}")
    suspend fun nonBlockingCoroutineApi(@PathVariable ioDelay: Long): ResponseEntity<List<DummyResponse>> {
        // kotlinx.coroutines.reactor extension
        // suspend fun <T> Mono<T>.awaitSingle(): T
        val resp = nonBlockingIO(ioDelay).awaitSingle()
        val result = dependentNonBlockingIO(ioDelay, resp).awaitSingle()
        return ResponseEntity.ok(result)
    }

    @RequestMapping("/webflux-non-blocking-reactor-structured-concurrency/{ioDelay}")
    fun nonBlockingStructuredTaskScopeApi(@PathVariable ioDelay: Long): Mono<ResponseEntity<List<DummyResponse>>> {
        return Mono.create { observer ->
            StructuredTaskScope.ShutdownOnFailure().use { scope ->
                val future = scope.fork {
                    // turning non-blocking to blocking in a virtual thread
                    val resp = nonBlockingIO(ioDelay)
                        .subscribeOn(Schedulers.fromExecutor(vtExecutor))
                        .block()!!
                    val result = dependentNonBlockingIO(ioDelay, resp)
                        .subscribeOn(Schedulers.fromExecutor(vtExecutor))
                        .block()!!
                    ResponseEntity.ok(result)
                }

                // Wait for all threads to finish or the task scope to shut down.
                // This method waits until all threads started in the task scope finish execution
                scope.join()
                scope.throwIfFailed()

                observer.success(future.resultNow())
            }
        }.subscribeOn(Schedulers.fromExecutor(vtExecutor))
    }

}
