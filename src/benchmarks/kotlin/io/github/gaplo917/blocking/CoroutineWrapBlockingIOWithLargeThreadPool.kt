package io.github.gaplo917.blocking

import io.github.gaplo917.blocking.helper.BlockingOps
import io.github.gaplo917.common.InvocationBenchmark
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*

// _000_withContext_wrap_blocking_io_custom_thread_pool   100   24986.589 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool   200   52647.982 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool   300   79475.491 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool   400  106513.655 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool   500  125844.288 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool   600   93626.857 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool   700   72438.499 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool   800   55050.167 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool   900   50290.195 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool  1000   42041.801 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool  2500   15486.241 ops/s
// _000_withContext_wrap_blocking_io_custom_thread_pool  5000    8380.684 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(50000)
class CoroutineWrapBlockingIOWithLargeThreadPool : BlockingOps, InvocationBenchmark {

  override val ioDelay: Long = 3

  override val invocations: Int = 50000

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
