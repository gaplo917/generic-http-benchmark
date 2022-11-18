package io.github.jmh.compute

import io.github.jmh.common.BenchmarkComputeMode
import io.github.jmh.common.InvocationBenchmark
import io.github.jmh.compute.helper.ComputeOps
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

// _000_sync_raw_compute_ops                                    no_compute 136509045.494 ops/s
// _000_sync_raw_compute_ops                                       compute     33514.211 ops/s
// _001_sequential_coroutine_wrap_compute_ops                   no_compute  36783805.903 ops/s
// _001_sequential_coroutine_wrap_compute_ops                      compute     35799.945 ops/s
// _002_sequential_future_wrap_compute_ops                      no_compute    678875.752 ops/s
// _002_sequential_future_wrap_compute_ops                         compute     27936.218 ops/s
// _003_sequential_reactor_mono_wrap_compute_ops                no_compute  17302289.189 ops/s
// _003_sequential_reactor_mono_wrap_compute_ops                   compute     34624.260 ops/s
// _004_sequential_reactor_flux_wrap_compute_ops                no_compute   2459934.981 ops/s
// _004_sequential_reactor_flux_wrap_compute_ops                   compute     33687.755 ops/s
// _011_sequential_coroutine_wrap_compute_ops_and_transform     no_compute  36001929.211 ops/s
// _011_sequential_coroutine_wrap_compute_ops_and_transform        compute     34979.415 ops/s
// _012_sequential_future_wrap_compute_ops_and_transform        no_compute    809452.461 ops/s
// _012_sequential_future_wrap_compute_ops_and_transform           compute     28195.115 ops/s
// _013_sequential_reactor_mono_wrap_compute_ops_and_transform  no_compute  13958308.799 ops/s
// _013_sequential_reactor_mono_wrap_compute_ops_and_transform     compute     34514.640 ops/s
// _014_sequential_reactor_flux_wrap_compute_ops_and_transform  no_compute   2160431.805 ops/s
// _014_sequential_reactor_flux_wrap_compute_ops_and_transform     compute     35162.439 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(32)
class SequentialChainComputeOps : ComputeOps, InvocationBenchmark {
  override val invocations: Int = 32

  @Param(value = ["no_compute", "compute"]) lateinit var computeMode: String

  override val benchmarkComputeMode: BenchmarkComputeMode by lazy {
    BenchmarkComputeMode.from(computeMode)
  }

  @Benchmark fun _000_sync_raw_compute_ops() = syncInvocationBenchmark { computeOps() }

  @Benchmark
  fun _001_sequential_coroutine_wrap_compute_ops() = sequentialCoroutineInvocationBenchmark {
    suspendCoroutine { it.resume(computeOps()) }
  }

  @Benchmark
  fun _002_sequential_future_wrap_compute_ops() = sequentialFutureInvocationBenchmark {
    CompletableFuture.supplyAsync { computeOps() }
  }

  @Benchmark
  fun _003_sequential_reactor_mono_wrap_compute_ops() = sequentialReactorMonoInvocationBenchmark {
    Mono.create { observer -> observer.success(computeOps()) }
  }

  @Benchmark
  fun _004_sequential_reactor_flux_wrap_compute_ops() = sequentialReactorFluxInvocationBenchmark {
    Flux.create { emitter ->
      emitter.next(computeOps())
      emitter.complete()
    }
  }

  @Benchmark
  fun _011_sequential_coroutine_wrap_no_ops_and_transform() =
    sequentialCoroutineInvocationBenchmark {
      suspendCoroutine { it.resume(computeOps()) }
      1
    }

  @Benchmark
  fun _012_sequential_future_wrap_no_ops_and_transform() = sequentialFutureInvocationBenchmark {
    CompletableFuture.supplyAsync { computeOps() }.thenAccept { 1 }
  }

  @Benchmark
  fun _013_sequential_reactor_mono_wrap_no_ops_and_transform() =
    sequentialReactorMonoInvocationBenchmark {
      Mono.create { observer -> observer.success(computeOps()) }.map { 1 }
    }

  @Benchmark
  fun _014_sequential_reactor_flux_wrap_no_ops_and_transform() =
    sequentialReactorFluxInvocationBenchmark {
      Flux.create { emitter ->
          emitter.next(computeOps())
          emitter.complete()
        }
        .map { 1 }
    }
}
