package io.github.gaplo917.blocking

import io.github.gaplo917.blocking.helper.BlockingOps
import io.github.gaplo917.common.DispatcherParameterConversion
import io.github.gaplo917.common.InvocationBenchmark
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*

// _001_suspendCoroutine_wrap_blocking_io   Unconfined    246.991 ops/s
// _001_suspendCoroutine_wrap_blocking_io           IO  14629.352 ops/s
// _001_suspendCoroutine_wrap_blocking_io      Default   2555.047 ops/s
// _002_withContext_wrap_blocking_io        Unconfined    245.590 ops/s
// _002_withContext_wrap_blocking_io                IO  14906.389 ops/s
// _002_withContext_wrap_blocking_io           Default   2415.895 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(300)
class CoroutineWrapBlockingIOWithDispatcher :
  BlockingOps, InvocationBenchmark, DispatcherParameterConversion {

  override val ioDelay: Long = 3

  override val invocations: Int = 300

  @Param(value = ["Unconfined", "IO", "Default"]) lateinit var dispatcher: String

  lateinit var coroutineDispatcher: CoroutineDispatcher

  @Setup
  fun setUp() {
    coroutineDispatcher = convertDispatcherParam(dispatcher)
  }

  @Benchmark
  fun _001_suspendCoroutine_wrap_blocking_io() =
    parallelCoroutineInvocationBenchmark(coroutineDispatcher) {
      suspendCoroutine { it.resume(blockingIO()) }
    }

  @Benchmark
  fun _002_withContext_wrap_blocking_io() = parallelCoroutineInvocationBenchmark {
    withContext(coroutineDispatcher) { blockingIO() }
  }
}
