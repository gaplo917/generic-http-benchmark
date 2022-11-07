package io.github.gaplo917.noops

import io.github.gaplo917.common.DispatcherParameterConversion
import io.github.gaplo917.common.InvocationBenchmark
import io.github.gaplo917.noops.helper.NoOps
import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineDispatcher
import org.openjdk.jmh.annotations.*

// _001_parallel_coroutine_no_ops   Unconfined 23615593.459 ops/s
// _001_parallel_coroutine_no_ops           IO   240052.896 ops/s
// _001_parallel_coroutine_no_ops      Default  1075884.222 ops/s
// _001_parallel_coroutine_no_ops SingleThread  3494663.343 ops/s
// _001_parallel_coroutine_no_ops    TwoThread  4008912.911 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(500_000)
class CoroutineDispatcher : NoOps, InvocationBenchmark, DispatcherParameterConversion {
  override val invocations: Int = 500_000

  @Param(value = ["Unconfined", "IO", "Default", "SingleThread", "TwoThread"])
  lateinit var dispatcher: String

  lateinit var coroutineDispatcher: CoroutineDispatcher

  @Setup
  fun setUp() {
    coroutineDispatcher = convertDispatcherParam(dispatcher)
  }

  @Benchmark
  fun _001_parallel_coroutine_no_ops() =
    parallelCoroutineInvocationBenchmark(coroutineDispatcher) { noOps() }
}
