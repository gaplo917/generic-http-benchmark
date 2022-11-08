package io.github.gaplo917.compute

import io.github.gaplo917.common.BenchmarkComputeMode
import io.github.gaplo917.common.DispatcherParameterConversion
import io.github.gaplo917.common.InvocationBenchmark
import io.github.gaplo917.compute.helper.ComputeOps
import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineDispatcher
import org.openjdk.jmh.annotations.*

// _001_parallel_coroutine_no_ops   Unconfined 23615593.459 ops/s
// _001_parallel_coroutine_no_ops           IO   240052.896 ops/s
// _001_parallel_coroutine_no_ops      Default  1075884.222 ops/s
// _001_parallel_coroutine_no_ops SingleThread  3494663.343 ops/s
// _001_parallel_coroutine_no_ops    TwoThread  4008912.911 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(50_000)
class CoroutineDispatcher : ComputeOps, InvocationBenchmark, DispatcherParameterConversion {
  override val invocations: Int = 50_000

  @Param(value = ["no_compute", "compute"]) lateinit var computeMode: String

  override val benchmarkComputeMode: BenchmarkComputeMode by lazy {
    BenchmarkComputeMode.from(computeMode)
  }

  @Param(value = ["Unconfined", "IO", "Default", "SingleThread", "TwoThread"])
  lateinit var dispatcher: String

  lateinit var coroutineDispatcher: CoroutineDispatcher

  @Setup
  fun setUp() {
    coroutineDispatcher = convertDispatcherParam(dispatcher)
  }

  @Benchmark
  fun _001_parallel_coroutine_compute_ops() =
    parallelCoroutineInvocationBenchmark(coroutineDispatcher) { computeOps() }
}
