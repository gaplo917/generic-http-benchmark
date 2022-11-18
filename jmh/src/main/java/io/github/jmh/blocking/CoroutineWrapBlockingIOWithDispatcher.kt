package io.github.jmh.blocking

import io.github.jmh.blocking.helper.BlockingOps
import io.github.jmh.common.BenchmarkComputeMode
import io.github.jmh.common.DispatcherParameterConversion
import io.github.jmh.common.InvocationBenchmark
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*

// _001_suspendCoroutine_wrap_blocking_io  no_compute  Unconfined    249.961 ops/s
// _001_suspendCoroutine_wrap_blocking_io  no_compute          IO  14886.661 ops/s
// _001_suspendCoroutine_wrap_blocking_io  no_compute     Default   2501.006 ops/s
// _001_suspendCoroutine_wrap_blocking_io     compute  Unconfined    237.456 ops/s
// _001_suspendCoroutine_wrap_blocking_io     compute          IO  14469.598 ops/s
// _001_suspendCoroutine_wrap_blocking_io     compute     Default   2428.850 ops/s
// _002_withContext_wrap_blocking_io       no_compute  Unconfined    247.716 ops/s
// _002_withContext_wrap_blocking_io       no_compute          IO  14619.053 ops/s
// _002_withContext_wrap_blocking_io       no_compute     Default   2609.658 ops/s
// _002_withContext_wrap_blocking_io          compute  Unconfined    235.293 ops/s
// _002_withContext_wrap_blocking_io          compute          IO  14496.140 ops/s
// _002_withContext_wrap_blocking_io          compute     Default   2534.807 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(300)
class CoroutineWrapBlockingIOWithDispatcher :
  BlockingOps, InvocationBenchmark, DispatcherParameterConversion {

  override val ioDelay: Long = 3

  override val invocations: Int = 300

  @Param(value = ["no_compute", "compute"]) lateinit var computeMode: String

  override val benchmarkComputeMode: BenchmarkComputeMode by lazy {
    BenchmarkComputeMode.from(computeMode)
  }

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
