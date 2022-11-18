package io.github.gaplo917.springmvc

import io.github.gaplo917.springmvc.data.DummyResponse
import jdk.incubator.concurrent.StructuredTaskScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future

@Controller
class BlockingIOController {
    private val virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor()
    private val virtualThreadCoroutineDispatcher = virtualThreadExecutor.asCoroutineDispatcher()

    private fun blockingIO(ioDelay: Long): DummyResponse {
        Thread.sleep(ioDelay)
        return DummyResponse.dummy()
    }

    private fun dependentBlockingIO(ioDelay: Long, resp: DummyResponse): List<DummyResponse> {
        Thread.sleep(ioDelay)
        return listOf(resp, DummyResponse.dummy())
    }

    @RequestMapping("/mvc-blocking/{ioDelay}")
    fun blockingApi(@PathVariable ioDelay: Long): ResponseEntity<List<DummyResponse>> {
        val resp1 = blockingIO(ioDelay)
        val result = dependentBlockingIO(ioDelay, resp1)
        return ResponseEntity.ok(result)
    }

    @RequestMapping("/mvc-blocking-vt/{ioDelay}")
    fun blockingNativeVirtualThreadApi(@PathVariable ioDelay: Long): ResponseEntity<List<DummyResponse>> {
        val resp = StructuredTaskScope.ShutdownOnFailure().use { scope ->
            val future1 = scope.fork {
                val resp1 = blockingIO(ioDelay)
                dependentBlockingIO(ioDelay, resp1)
            }

            // Wait for all threads to finish or the task scope to shut down.
            // This method waits until all threads started in the task scope finish execution
            scope.join()
            scope.throwIfFailed()

            future1.resultNow()
        }
        return ResponseEntity.ok(resp)
    }


    @RequestMapping("/mvc-blocking-vt-future/{ioDelay}")
    fun blockingFutureInVTApi(@PathVariable ioDelay: Long): Future<ResponseEntity<List<DummyResponse>>> {
        return CompletableFuture.supplyAsync(
            {
                val resp1 = blockingIO(ioDelay)
                dependentBlockingIO(ioDelay, resp1)
            }, virtualThreadExecutor
        ).thenApplyAsync { result -> ResponseEntity.ok(result) }
    }

    @RequestMapping("/mvc-blocking-vt-coroutine/{ioDelay}")
    suspend fun blockingSuspendInVTApi(@PathVariable ioDelay: Long): ResponseEntity<List<DummyResponse>> =
        withContext(virtualThreadCoroutineDispatcher) {
            val resp1 = blockingIO(ioDelay)
            val result = dependentBlockingIO(ioDelay, resp1)
            ResponseEntity.ok(result)
        }

}
