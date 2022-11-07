package io.github.gaplo917.nonblocking

import io.github.gaplo917.common.InvocationBenchmark
import io.github.gaplo917.nonblocking.helper.NonBlockingOps
import io.netty.channel.nio.NioEventLoopGroup
import java.util.concurrent.ScheduledExecutorService
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*

// _000_parallel_no_wrap_callback                              3384961.627 ops/s
// _001_parallel_suspendCoroutine_wrap_callback                2952222.580 ops/s
// _002_parallel_suspendCancellableCoroutine_wrap_callback     3025397.691 ops/s
// _003_parallel_suspendCoroutine_global_launch_wrap_callback  2790018.775 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(500_000)
class CoroutineWrapNonBlockingIO : NonBlockingOps, InvocationBenchmark {
  override lateinit var controlledExecutor: ScheduledExecutorService

  override val ioDelay: Long = 3

  override val invocations: Int = 500_000

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
