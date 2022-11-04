package io.github.gaplo917.nonblocking

import io.github.gaplo917.helper.NonBlockingOps
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*

@State(Scope.Benchmark)
@Threads(1)
@OperationsPerInvocation(50000)
class CoroutineSuspendNonBlockingWrapper : NonBlockingOps {

  override lateinit var controlledExecutor: ScheduledExecutorService

  override val ioDelay: Long = 3

  override val invocations: Int = 50000

  @Setup
  fun setup() {
    controlledExecutor = Executors.newSingleThreadScheduledExecutor()
  }

  @TearDown
  fun tearDown() {
    controlledExecutor.shutdown()
  }

  @Benchmark
  fun _000_baseline() = parallelBaselineBenchmark()

  @Benchmark fun _001_suspend() = parallelCoBenchmark { suspendNoOps() }

  @Benchmark fun _002_suspendCancellable() = parallelCoBenchmark { suspendCancellableNoOps() }

  @Benchmark fun _003_suspendLaunch() = parallelCoBenchmark { suspendLaunchNoOps() }

  suspend fun suspendNoOps() = suspendCoroutine { nonBlockingOps { it.resume(Unit) } }

  suspend fun suspendCancellableNoOps() = suspendCancellableCoroutine {
    nonBlockingOps { it.resume(Unit) }
  }

  suspend fun suspendLaunchNoOps() = suspendCoroutine {
    GlobalScope.launch(Dispatchers.Unconfined) { nonBlockingOps { it.resume(Unit) } }
  }
}
