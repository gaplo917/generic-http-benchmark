package io.github.gaplo917.blocking

import io.github.gaplo917.blocking.helper.BlockingOps
import io.github.gaplo917.common.DispatcherParameterConversion
import io.github.gaplo917.common.InvocationBenchmark
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*

// _000_suspendCoroutine_virtual_thread_wrap_blocking_io           Unconfined  912326.461  ops/s
// _000_suspendCoroutine_virtual_thread_wrap_blocking_io                   IO   66887.019  ops/s
// _000_suspendCoroutine_virtual_thread_wrap_blocking_io              Default  510363.187  ops/s
// _001_suspendCoroutine_virtual_thread_future_wrap_blocking_io    Unconfined  665297.794  ops/s
// _001_suspendCoroutine_virtual_thread_future_wrap_blocking_io            IO   70909.369  ops/s
// _001_suspendCoroutine_virtual_thread_future_wrap_blocking_io       Default  635846.630  ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(50000)
class CoroutineWrapBlockingIOWithJava19VirtualThread :
  BlockingOps, InvocationBenchmark, DispatcherParameterConversion {
  lateinit var executorService: ExecutorService

  override val ioDelay: Long = 3

  override val invocations: Int = 50000

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
