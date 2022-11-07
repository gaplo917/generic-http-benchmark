package io.github.gaplo917.noops

import io.github.gaplo917.common.InvocationBenchmark
import io.github.gaplo917.noops.helper.NoOps
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*

// _000_parallel_no_wrap_no_ops                             23493707.442 ops/s
// _001_parallel_suspendCoroutine_wrap_no_ops               16430349.112 ops/s
// _002_parallel_suspendCancellableCoroutine_wrap_no_ops    12439378.150 ops/s
// _003_parallel_suspendCoroutine_global_launch_wrap_no_ops  7450745.657 ops/s
// _004_parallel_coroutineScope_wrap_no_ops                 13759433.321 ops/s
// _005_parallel_supervisorScope_wrap_no_ops                13811444.384 ops/s
// _006_parallel_withContext_wrap_no_ops                     7174676.214 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(500_000)
class CoroutineWrapNoOps : NoOps, InvocationBenchmark {
  override val invocations: Int = 500_000

  @Benchmark fun _000_parallel_no_wrap_no_ops() = parallelCoroutineInvocationBenchmark { noOps() }

  @Benchmark
  fun _001_parallel_suspendCoroutine_wrap_no_ops() = parallelCoroutineInvocationBenchmark {
    suspendCoroutine { it.resume(noOps()) }
  }

  @Benchmark
  fun _002_parallel_suspendCancellableCoroutine_wrap_no_ops() =
    parallelCoroutineInvocationBenchmark {
      suspendCancellableCoroutine { it.resume(noOps()) }
    }

  @Benchmark
  fun _003_parallel_suspendCoroutine_global_launch_wrap_no_ops() =
    parallelCoroutineInvocationBenchmark {
      suspendCoroutine { GlobalScope.launch(Dispatchers.Unconfined) { it.resume(noOps()) } }
    }

  @Benchmark
  fun _004_parallel_coroutineScope_wrap_no_ops() = parallelCoroutineInvocationBenchmark {
    coroutineScope { noOps() }
  }

  @Benchmark
  fun _005_parallel_supervisorScope_wrap_no_ops() = parallelCoroutineInvocationBenchmark {
    supervisorScope { noOps() }
  }

  @Benchmark
  fun _006_parallel_withContext_wrap_no_ops() = parallelCoroutineInvocationBenchmark {
    withContext(Dispatchers.Unconfined) { noOps() }
  }
}
