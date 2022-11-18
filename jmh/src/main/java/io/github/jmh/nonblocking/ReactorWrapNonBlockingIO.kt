package io.github.jmh.nonblocking

import io.github.jmh.common.BenchmarkComputeMode
import io.github.jmh.common.InvocationBenchmark
import io.github.jmh.nonblocking.helper.NonBlockingOps
import io.netty.channel.nio.NioEventLoopGroup
import java.util.concurrent.ScheduledExecutorService
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

// _000_parallel_no_wrap_callback                    no_compute 2890949.315 ops/s
// _000_parallel_no_wrap_callback                       compute  261317.446 ops/s
// _001_parallel_mono_create_wrap_callback           no_compute 3326912.503 ops/s
// _001_parallel_mono_create_wrap_callback              compute  269650.945 ops/s
// _002_parallel_mono_defer_and_create_wrap_callback no_compute 3019475.518 ops/s
// _002_parallel_mono_defer_and_create_wrap_callback    compute  270232.089 ops/s
// _003_parallel_flux_create_wrap_callback           no_compute 2518142.790 ops/s
// _003_parallel_flux_create_wrap_callback              compute  248414.511 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(500_000)
class ReactorWrapNonBlockingIO : NonBlockingOps, InvocationBenchmark {
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
  fun _001_parallel_mono_create_wrap_callback() = parallelReactorMonoInvocationBenchmark {
    Mono.create { nonBlockingCallback { it.success(Unit) } }
  }

  @Benchmark
  fun _002_parallel_mono_defer_and_create_wrap_callback() = parallelReactorMonoInvocationBenchmark {
    Mono.defer { Mono.create { nonBlockingCallback { it.success(Unit) } } }
  }

  @Benchmark
  fun _003_parallel_flux_create_wrap_callback() = parallelReactorMonoInvocationBenchmark {
    Flux.create {
        nonBlockingCallback {
          it.next(Unit)
          it.complete()
        }
      }
      .last()
  }
}
