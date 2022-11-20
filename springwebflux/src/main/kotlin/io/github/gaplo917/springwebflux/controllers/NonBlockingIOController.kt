package io.github.gaplo917.springwebflux.controllers

import io.github.gaplo917.springwebflux.data.DummyResponse
import io.github.gaplo917.springwebflux.services.IOService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class NonBlockingIOController(private val ioService: IOService) {
    @RequestMapping("/webflux-nio-coroutine/{ioDelay}")
    suspend fun nonBlockingCoroutineApi(@PathVariable ioDelay: Long): ResponseEntity<List<DummyResponse>> {
        // kotlinx.coroutines.reactor extension
        // suspend fun <T> Mono<T>.awaitSingle(): T
        val resp = ioService.nonBlockingIO(ioDelay).awaitSingle()
        val result = ioService.dependentNonBlockingIO(ioDelay, resp).awaitSingle()
        return ResponseEntity.ok(result)
    }

    @RequestMapping("/webflux-nio-coroutine-parallel/{ioDelay}")
    suspend fun nonBlockingCoroutineParallelIOApi(@PathVariable ioDelay: Long): ResponseEntity<List<DummyResponse>> =
        coroutineScope {
            // kotlinx.coroutines.reactor extension
            // suspend fun <T> Mono<T>.awaitSingle(): T
            val resp1 = async { ioService.nonBlockingIO(ioDelay).awaitSingle() }
            val resp2 = async { ioService.nonBlockingIO(ioDelay).awaitSingle() }
            val result = listOf(resp1.await(), resp2.await())
            ResponseEntity.ok(result)
        }
}
