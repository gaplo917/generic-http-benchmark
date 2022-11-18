package io.github.gaplo917.springwebflux

import io.github.gaplo917.springwebflux.data.DummyResponse
import jdk.incubator.concurrent.StructuredTaskScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Executors

@Controller
class BlockingIOController {
    private val vtExecutor = Executors.newVirtualThreadPerTaskExecutor()
    private val vtCoroutineDispatcher = vtExecutor.asCoroutineDispatcher()

    fun blockingIO(ioDelay: Long): DummyResponse {
        Thread.sleep(ioDelay)
        return DummyResponse.dummy()
    }

    private fun dependentBlockingIO(ioDelay: Long, resp: DummyResponse): List<DummyResponse> {
        Thread.sleep(ioDelay)
        return listOf(resp, DummyResponse.dummy())
    }

    @RequestMapping("/webflux-blocking/{ioDelay}")
    fun blockingApi(@PathVariable ioDelay: Long): ResponseEntity<List<DummyResponse>> {
        val resp1 = blockingIO(ioDelay)
        val result = dependentBlockingIO(ioDelay, resp1)
        return ResponseEntity.ok(result)
    }

    @RequestMapping("/webflux-blocking-structured-concurrency/{ioDelay}")
    fun blockingNativeVirtualThreadApi(@PathVariable ioDelay: Long): Mono<ResponseEntity<List<DummyResponse>>> {
        return Mono.create { observer ->
            StructuredTaskScope.ShutdownOnFailure().use { scope ->
                val f = scope.fork {
                    val resp1 = blockingIO(ioDelay)
                    dependentBlockingIO(ioDelay, resp1)
                }
                scope.join()
                scope.throwIfFailed()
                observer.success(f.resultNow())
            }
        }.subscribeOn(Schedulers.fromExecutor(vtExecutor))
            .map { result -> ResponseEntity.ok(result) }
    }

    @RequestMapping("/webflux-blocking-vt-reactor/{ioDelay}")
    fun blockingVirtualThreadReactorApi(@PathVariable ioDelay: Long): Mono<ResponseEntity<List<DummyResponse>>> {
        return Mono.create { observer ->
            val resp1 = blockingIO(ioDelay)
            observer.success(dependentBlockingIO(ioDelay, resp1))
        }.subscribeOn(Schedulers.fromExecutor(vtExecutor))
            .map { result -> ResponseEntity.ok(result) }
    }

    @RequestMapping("/webflux-blocking-vt-coroutine/{ioDelay}")
    suspend fun blockingVirtualThreadApi(@PathVariable ioDelay: Long): ResponseEntity<List<DummyResponse>> {
        return withContext(vtCoroutineDispatcher) {
            val resp1 = blockingIO(ioDelay)
            val result = dependentBlockingIO(ioDelay, resp1)
            ResponseEntity.ok(result)
        }
    }
}
