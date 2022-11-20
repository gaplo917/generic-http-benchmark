package io.github.gaplo917.springmvc.controllers

import io.github.gaplo917.springmvc.data.DummyResponse
import io.github.gaplo917.springmvc.services.IOService
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
    private val virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor()
    private val virtualThreadCoroutineDispatcher = virtualThreadExecutor.asCoroutineDispatcher()

    @RequestMapping("/mvc-bio-coroutine-in-vt/{ioDelay}")
    suspend fun blockingCoroutineInVTApi(@PathVariable ioDelay: Long): ResponseEntity<List<DummyResponse>> =
        withContext(virtualThreadCoroutineDispatcher) {
            val resp1 = ioService.blockingIO(ioDelay)
            val result = ioService.dependentBlockingIO(ioDelay, resp1)
            ResponseEntity.ok(result)
        }

    @RequestMapping("/mvc-bio-coroutine-parallel/{ioDelay}")
    suspend fun blockingCoroutineInVTParallelIOApi(@PathVariable ioDelay: Long): ResponseEntity<List<DummyResponse>> =
        withContext(virtualThreadCoroutineDispatcher) {
            val resp1 = async { ioService.blockingIO(ioDelay) }
            val resp2 = async { ioService.blockingIO(ioDelay) }
            ResponseEntity.ok(listOf(resp1.await(), resp2.await()))
        }

}
