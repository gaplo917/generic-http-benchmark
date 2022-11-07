package io.github.gaplo917.blocking

import io.github.gaplo917.blocking.helper.BlockingOps
import io.github.gaplo917.common.InvocationBenchmark
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*

// _000_raw_virtual_thread_baseline           912834.218 ops/s
// _001_raw_virtual_thread_completableFuture 743404.974 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(50000)
class Java19VirtualThreadWrapBlockingIO : BlockingOps, InvocationBenchmark {
  lateinit var executorService: ExecutorService

  override val ioDelay: Long = 3

  override val invocations: Int = 50000

  @Setup
  fun setUp() {
    executorService = Executors.newVirtualThreadPerTaskExecutor()
  }

  @Benchmark
  fun _000_raw_virtual_thread_baseline() = parallelCallbackInvocationBenchmark { cb ->
    executorService.submit {
      blockingIO()
      cb()
    }
  }

  @Benchmark
  fun _001_raw_virtual_thread_completableFuture() = parallelFutureInvocationBenchmark {
    CompletableFuture.supplyAsync({ blockingIO() }, executorService)
  }
}
