package io.github.gaplo917.springmvc.controllers

import io.github.gaplo917.springmvc.data.DummyResponse
import io.github.gaplo917.springmvc.services.IOService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class NonBlockingIOController(private val ioService: IOService) {

    @RequestMapping("/mvc-nio-coroutine/{ioDelay}")
    suspend fun nonBlockingCoroutineApi(@PathVariable ioDelay: Long): ResponseEntity<List<DummyResponse>> {
        // kotlinx-coroutines-jdk8 extension
        // `suspend fun <T> CompletionStage<T>.await(): T`
        val resp = ioService.nonBlockingIO(ioDelay).await()
        val result = ioService.dependentNonBlockingIO(ioDelay, resp).await()
        return ResponseEntity.ok(result)
    }

    @RequestMapping("/mvc-nio-coroutine-parallel/{ioDelay}")
    suspend fun nonBlockingCoroutineParallelIOApi(@PathVariable ioDelay: Long): ResponseEntity<List<DummyResponse>> {
        // kotlinx-coroutines-jdk8 extension
        // `suspend fun <T> CompletionStage<T>.await(): T`
        return coroutineScope {
            val resp1 = async { ioService.nonBlockingIO(ioDelay).await() }
            val resp2 = async { ioService.nonBlockingIO(ioDelay).await() }
            ResponseEntity.ok(listOf(resp1.await(), resp2.await()))
        }
    }

}
