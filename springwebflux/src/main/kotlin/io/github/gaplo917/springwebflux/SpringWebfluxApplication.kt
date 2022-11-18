package io.github.gaplo917.springwebflux

import io.github.gaplo917.springwebflux.data.DummyResponse
import jdk.incubator.concurrent.StructuredTaskScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SpringBootApplication
class SpringWebfluxApplication

@Controller
class BenchmarkController {
    private val scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val vtExecutor = Executors.newVirtualThreadPerTaskExecutor()
    private val vtCoroutineDispatcher = vtExecutor.asCoroutineDispatcher()

    fun blockingIO(ioDelay: Long): DummyResponse {
        Thread.sleep(ioDelay)
        return DummyResponse.dummy()
    }

    fun nonBlockingIO(ioDelay: Long, cb: (DummyResponse) -> Unit) {
        scheduledExecutorService.schedule({ cb(DummyResponse.dummy()) }, ioDelay, TimeUnit.MILLISECONDS)
    }

    @RequestMapping("/blocking/{ioDelay}")
    fun blockingApi(@PathVariable ioDelay: Long): ResponseEntity<DummyResponse> {
        return ResponseEntity.ok(blockingIO(ioDelay))
    }

    fun nonBlockingReactor(ioDelay: Long): Mono<DummyResponse> {
        return Mono.create { observer ->
            nonBlockingIO(ioDelay) {
                observer.success(it)
            }
        }
    }

    @RequestMapping("/non-blocking-reactor/{ioDelay}")
    fun nonBlockingReactorApi(@PathVariable ioDelay: Long): Mono<ResponseEntity<DummyResponse>> {
        return nonBlockingReactor(ioDelay)
            .map { ResponseEntity.ok(it) }
    }

    suspend fun nonBlockingIOCoroutine(ioDelay: Long): DummyResponse = suspendCoroutine { cnt ->
        nonBlockingIO(ioDelay) {
            cnt.resume(it)
        }
    }

    @RequestMapping("/non-blocking-coroutine/{ioDelay}")
    suspend fun nonBlockingCoroutineApi(@PathVariable ioDelay: Long): ResponseEntity<DummyResponse> {
        return ResponseEntity.ok(nonBlockingIOCoroutine(ioDelay))
    }


    fun blockingVirtualThreadStructuredScope(ioDelay: Long): Mono<DummyResponse> {
        return Mono.create { observer ->
            StructuredTaskScope.ShutdownOnFailure().use { scope ->
                val f = scope.fork { blockingIO(ioDelay) }
                scope.join()
                scope.throwIfFailed()
                observer.success(f.resultNow())
            }
        }.subscribeOn(Schedulers.fromExecutor(vtExecutor))
    }

    @RequestMapping("/blocking-vt-structured-scope/{ioDelay}")
    fun blockingNativeVirtualThreadApi(@PathVariable ioDelay: Long): Mono<ResponseEntity<DummyResponse>> {
        return blockingVirtualThreadStructuredScope(ioDelay)
            .map { ResponseEntity.ok(it) }
    }

    fun blockingVirtualThreadReactor(ioDelay: Long): Mono<DummyResponse> {
        return Mono.create { observer ->
            observer.success(blockingIO(ioDelay))
        }.subscribeOn(Schedulers.fromExecutor(vtExecutor))
    }

    @RequestMapping("/blocking-vt-reactor/{ioDelay}")
    fun blockingVirtualThreadReactorApi(@PathVariable ioDelay: Long): Mono<ResponseEntity<DummyResponse>> {
        return blockingVirtualThreadReactor(ioDelay)
            .map { ResponseEntity.ok(it) }
    }

    suspend fun blockingVirtualThread(ioDelay: Long): DummyResponse = withContext(vtCoroutineDispatcher) {
        blockingIO(ioDelay)
    }

    @RequestMapping("/blocking-vt-coroutine/{ioDelay}")
    suspend fun blockingVirtualThreadApi(@PathVariable ioDelay: Long): ResponseEntity<DummyResponse> {
        return ResponseEntity.ok(blockingVirtualThread(ioDelay))
    }
}

fun main(args: Array<String>) {
    runApplication<SpringWebfluxApplication>(*args)
}
