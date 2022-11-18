package io.github.jmh.blocking

import io.github.jmh.blocking.helper.BlockingOps
import io.github.jmh.common.BenchmarkComputeMode
import io.github.jmh.common.InvocationBenchmark
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*

// _000_withContext_wrap_blocking_io_custom_thread_pool  no_compute  100   24013.245 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool  no_compute  200   51402.867 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool  no_compute  300   79086.174 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool  no_compute  400  106488.599 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool  no_compute  500  126859.570 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool  no_compute  600   88785.835 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool  no_compute  700   78829.045 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool  no_compute  800   56193.950 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool  no_compute  900   63302.611 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool  no_compute 1000   39562.443 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool  no_compute 2500   15242.552 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool  no_compute 5000    8231.099 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool     compute  100   23928.841 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool     compute  200   51254.746 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool     compute  300   78278.899 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool     compute  400  100709.397 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool     compute  500   83281.199 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool     compute  600   69856.135 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool     compute  700   58477.238 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool     compute  800   44493.068 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool     compute  900   40105.497 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool     compute 1000   39130.378 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool     compute 2500   15629.212 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool     compute 5000    8361.032 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(50000)
class CoroutineWrapBlockingIOWithLargeThreadPool : BlockingOps, InvocationBenchmark {

  override val ioDelay: Long = 3

  override val invocations: Int = 50000

  @Param(value = ["no_compute", "compute"]) lateinit var computeMode: String

  override val benchmarkComputeMode: BenchmarkComputeMode by lazy {
    BenchmarkComputeMode.from(computeMode)
  }

  lateinit var coroutineDispatcher: CoroutineDispatcher

  @Param(
    value =
      [
        "100",
        "200",
        "300",
        "400",
        "500",
        "600",
        "700",
        "800",
        "900",
        "1000",
        "2500",
        "5000",
      ]
  )
  lateinit var threads: String

  @Setup
  fun setUp() {
    coroutineDispatcher = newFixedThreadPoolContext(threads.toInt(), "coroutine-${threads}")
  }

  @Benchmark
  fun _000_withContext_wrap_blocking_io_custom_thread_pool() =
    parallelCoroutineInvocationBenchmark {
      withContext(coroutineDispatcher) { blockingIO() }
    }
}
