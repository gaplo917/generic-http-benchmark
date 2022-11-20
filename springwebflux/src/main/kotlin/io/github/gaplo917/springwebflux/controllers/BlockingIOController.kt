package io.github.gaplo917.springwebflux.controllers

import io.github.gaplo917.springwebflux.data.DummyResponse
import io.github.gaplo917.springwebflux.services.IOService
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.util.concurrent.Executors

@Controller
class BlockingIOController(private val ioService: IOService) {
    private val vtExecutor = Executors.newVirtualThreadPerTaskExecutor()
    private val vtCoroutineDispatcher = vtExecutor.asCoroutineDispatcher()

    @RequestMapping("/webflux-bio-coroutine-in-vt/{ioDelay}")
    suspend fun blockingCoroutineInApi(@PathVariable ioDelay: Long): ResponseEntity<List<DummyResponse>> {
        return withContext(vtCoroutineDispatcher) {
            val resp1 = ioService.blockingIO(ioDelay)
            val result = ioService.dependentBlockingIO(ioDelay, resp1)
            ResponseEntity.ok(result)
        }
    }

    @RequestMapping("/webflux-bio-coroutine-in-vt-parallel/{ioDelay}")
    suspend fun blockingCoroutineInVTParallelIOApi(@PathVariable ioDelay: Long): ResponseEntity<List<DummyResponse>> {
        return withContext(vtCoroutineDispatcher) {
            val resp1 = async { ioService.blockingIO(ioDelay) }
            val resp2 = async { ioService.blockingIO(ioDelay) }
            val result = listOf(resp1.await(), resp2.await())
            ResponseEntity.ok(result)
        }
    }
}
