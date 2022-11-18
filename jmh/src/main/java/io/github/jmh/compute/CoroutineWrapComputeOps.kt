package io.github.jmh.compute

import io.github.jmh.common.BenchmarkComputeMode
import io.github.jmh.common.InvocationBenchmark
import io.github.jmh.compute.helper.ComputeOps
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*

// _000_parallel_no_wrap_compute_ops                               no_compute   913119.324  ops/s
// _000_parallel_no_wrap_compute_ops                                  compute   240170.074  ops/s
// _001_parallel_suspendCoroutine_wrap_compute_ops                 no_compute  1095790.778  ops/s
// _001_parallel_suspendCoroutine_wrap_compute_ops                    compute   245436.248  ops/s
// _002_parallel_suspendCancellableCoroutine_wrap_compute_ops      no_compute  1099320.921  ops/s
// _002_parallel_suspendCancellableCoroutine_wrap_compute_ops         compute   221831.695  ops/s
// _003_parallel_suspendCoroutine_global_launch_wrap_compute_ops   no_compute   782635.602  ops/s
// _003_parallel_suspendCoroutine_global_launch_wrap_compute_ops      compute   210180.186  ops/s
// _004_parallel_coroutineScope_wrap_compute_ops                   no_compute  1142420.185  ops/s
// _004_parallel_coroutineScope_wrap_compute_ops                      compute   242342.766  ops/s
// _005_parallel_supervisorScope_wrap_compute_ops                  no_compute   917080.464  ops/s
// _005_parallel_supervisorScope_wrap_compute_ops                     compute   234687.461  ops/s
// _006_parallel_withContext_wrap_compute_ops                      no_compute  1011340.760  ops/s
// _006_parallel_withContext_wrap_compute_ops                         compute   232535.077  ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(50_000)
class CoroutineWrapComputeOps : ComputeOps, InvocationBenchmark {
  override val invocations: Int = 50_000

  @Param(value = ["no_compute", "compute"]) lateinit var computeMode: String

  override val benchmarkComputeMode: BenchmarkComputeMode by lazy {
    BenchmarkComputeMode.from(computeMode)
  }

  @Benchmark
  fun _000_parallel_no_wrap_compute_ops() =
    parallelCoroutineInvocationBenchmark(Dispatchers.Default) { computeOps() }

  @Benchmark
  fun _001_parallel_suspendCoroutine_wrap_compute_ops() =
    parallelCoroutineInvocationBenchmark(Dispatchers.Default) {
      runBlocking {  }
      suspendCoroutine { it.resume(computeOps()) }
    }

  @Benchmark
  fun _002_parallel_suspendCancellableCoroutine_wrap_compute_ops() =
    parallelCoroutineInvocationBenchmark(Dispatchers.Default) {
      suspendCancellableCoroutine { it.resume(computeOps()) }
    }

  @Benchmark
  fun _003_parallel_suspendCoroutine_global_launch_wrap_compute_ops() =
    parallelCoroutineInvocationBenchmark(Dispatchers.Default) {
      suspendCoroutine { GlobalScope.launch(Dispatchers.Unconfined) { it.resume(computeOps()) } }
    }

  @Benchmark
  fun _004_parallel_coroutineScope_wrap_compute_ops() =
    parallelCoroutineInvocationBenchmark(Dispatchers.Default) { coroutineScope { computeOps() } }

  @Benchmark
  fun _005_parallel_supervisorScope_wrap_compute_ops() =
    parallelCoroutineInvocationBenchmark(Dispatchers.Default) { supervisorScope { computeOps() } }

  @Benchmark
  fun _006_parallel_withContext_wrap_compute_ops() =
    parallelCoroutineInvocationBenchmark(Dispatchers.Default) {
      withContext(Dispatchers.Unconfined) { computeOps() }
    }
}
