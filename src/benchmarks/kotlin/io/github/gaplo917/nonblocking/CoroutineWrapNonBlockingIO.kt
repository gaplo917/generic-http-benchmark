package io.github.gaplo917.nonblocking

import io.github.gaplo917.common.BenchmarkComputeMode
import io.github.gaplo917.common.InvocationBenchmark
import io.github.gaplo917.nonblocking.helper.NonBlockingOps
import io.netty.channel.nio.NioEventLoopGroup
import java.util.concurrent.ScheduledExecutorService
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*

// _000_parallel_no_wrap_callback                             no_compute  3036565.352 ops/s
// _000_parallel_no_wrap_callback                                compute   262693.354 ops/s
// _001_parallel_suspendCoroutine_wrap_callback               no_compute  2946727.145 ops/s
// _001_parallel_suspendCoroutine_wrap_callback                  compute   255792.764 ops/s
// _002_parallel_suspendCancellableCoroutine_wrap_callback    no_compute  2960560.612 ops/s
// _002_parallel_suspendCancellableCoroutine_wrap_callback       compute   233261.863 ops/s
// _003_parallel_suspendCoroutine_global_launch_wrap_callback no_compute  2725982.888 ops/s
// _003_parallel_suspendCoroutine_global_launch_wrap_callback    compute   267764.468 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(500_000)
class CoroutineWrapNonBlockingIO : NonBlockingOps, InvocationBenchmark {
  override lateinit var controlledExecutor: ScheduledExecutorService

  override val ioDelay: Long = 3

  override val invocations: Int = 500_000

  @Param(value = ["no_compute", "compute"]) lateinit var computeMode: String

  override val benchmarkComputeMode: BenchmarkComputeMode by lazy {
    BenchmarkComputeMode.from(computeMode)
  }

  @Setup
  fun setup() {
    controlledExecutor = NioEventLoopGroup()
  }

  @TearDown
  fun tearDown() {
    controlledExecutor.shutdown()
  }

  @Benchmark
  fun _000_parallel_no_wrap_callback() = parallelCallbackInvocationBenchmark(::nonBlockingCallback)

  @Benchmark
  fun _001_parallel_suspendCoroutine_wrap_callback() = parallelCoroutineInvocationBenchmark {
    suspendCoroutine { nonBlockingCallback { it.resume(Unit) } }
  }

  @Benchmark
  fun _002_parallel_suspendCancellableCoroutine_wrap_callback() =
    parallelCoroutineInvocationBenchmark {
      suspendCancellableCoroutine { nonBlockingCallback { it.resume(Unit) } }
    }

  @Benchmark
  fun _003_parallel_suspendCoroutine_global_launch_wrap_callback() =
    parallelCoroutineInvocationBenchmark {
      suspendCoroutine {
        GlobalScope.launch(Dispatchers.Unconfined) { nonBlockingCallback { it.resume(Unit) } }
      }
    }
}
