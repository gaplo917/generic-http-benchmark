package io.github.jmh.blocking

import io.github.jmh.blocking.helper.BlockingOps
import io.github.jmh.common.BenchmarkComputeMode
import io.github.jmh.common.DispatcherParameterConversion
import io.github.jmh.common.InvocationBenchmark
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*

// _000_suspendCoroutine_java19_v.t_wrap_blocking_io        no_compute Unconfined 851657.921 ops/s
// _000_suspendCoroutine_java19_v.t_wrap_blocking_io        no_compute         IO  73055.848 ops/s
// _000_suspendCoroutine_java19_v.t_wrap_blocking_io        no_compute    Default 512191.086 ops/s
// _000_suspendCoroutine_java19_v.t_wrap_blocking_io           compute Unconfined 200810.163 ops/s
// _000_suspendCoroutine_java19_v.t_wrap_blocking_io           compute         IO  76910.064 ops/s
// _000_suspendCoroutine_java19_v.t_wrap_blocking_io           compute    Default 169954.834 ops/s
// _001_suspendCoroutine_java19_v.t_future_wrap_blocking_io no_compute Unconfined 706353.077 ops/s
// _001_suspendCoroutine_java19_v.t_future_wrap_blocking_io no_compute         IO  70532.661 ops/s
// _001_suspendCoroutine_java19_v.t_future_wrap_blocking_io no_compute    Default 555121.584 ops/s
// _001_suspendCoroutine_java19_v.t_future_wrap_blocking_io    compute Unconfined 183018.369 ops/s
// _001_suspendCoroutine_java19_v.t_future_wrap_blocking_io    compute         IO 110182.270 ops/s
// _001_suspendCoroutine_java19_v.t_future_wrap_blocking_io    compute    Default 165866.731 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(50000)
class CoroutineWrapBlockingIOWithJava19VirtualThread :
  BlockingOps, InvocationBenchmark, DispatcherParameterConversion {
  lateinit var executorService: ExecutorService

  override val ioDelay: Long = 3

  override val invocations: Int = 50000

  @Param(value = ["no_compute", "compute"]) lateinit var computeMode: String

  override val benchmarkComputeMode: BenchmarkComputeMode by lazy {
    BenchmarkComputeMode.from(computeMode)
  }

  @Param(value = ["Unconfined", "IO", "Default"]) lateinit var dispatcher: String

  lateinit var coroutineDispatcher: CoroutineDispatcher

  @Setup
  fun setUp() {
    executorService = Executors.newVirtualThreadPerTaskExecutor()
    coroutineDispatcher = convertDispatcherParam(dispatcher)
  }

  @Benchmark
  fun _000_suspendCoroutine_java19_virtual_thread_wrap_blocking_io() =
    parallelCoroutineInvocationBenchmark(coroutineDispatcher) {
      suspendCoroutine { cnt ->
        executorService.submit {
          blockingIO()
          cnt.resume(Unit)
        }
      }
    }

  @Benchmark
  fun _001_suspendCoroutine_java19_virtual_thread_future_wrap_blocking_io() =
    parallelCoroutineInvocationBenchmark(coroutineDispatcher) {
      suspendCoroutine { cnt ->
        CompletableFuture.supplyAsync({ blockingIO() }, executorService).thenAcceptAsync {
          cnt.resume(Unit)
        }
      }
    }
}
