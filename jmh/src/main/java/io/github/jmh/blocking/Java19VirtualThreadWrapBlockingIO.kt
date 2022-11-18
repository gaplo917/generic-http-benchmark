package io.github.jmh.blocking

import io.github.jmh.blocking.helper.BlockingOps
import io.github.jmh.common.BenchmarkComputeMode
import io.github.jmh.common.InvocationBenchmark
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*

// _000_raw_virtual_thread_baseline          no_compute 895723.534 ops/s
// _000_raw_virtual_thread_baseline             compute 191208.390 ops/s
// _001_raw_virtual_thread_completableFuture no_compute 626203.613 ops/s
// _001_raw_virtual_thread_completableFuture    compute 166978.665 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(50000)
class Java19VirtualThreadWrapBlockingIO : BlockingOps, InvocationBenchmark {
  lateinit var executorService: ExecutorService

  override val ioDelay: Long = 3

  override val invocations: Int = 50000

  @Param(value = ["no_compute", "compute"]) lateinit var computeMode: String

  override val benchmarkComputeMode: BenchmarkComputeMode by lazy {
    BenchmarkComputeMode.from(computeMode)
  }

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
