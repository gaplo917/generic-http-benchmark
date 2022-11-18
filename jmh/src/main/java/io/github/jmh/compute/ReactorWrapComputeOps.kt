package io.github.jmh.compute

import io.github.jmh.common.BenchmarkComputeMode
import io.github.jmh.common.InvocationBenchmark
import io.github.jmh.compute.helper.ComputeOps
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

// _000_parallel_mono_just_wrap_compute_ops             no_compute 41850517.178 ops/s
// _000_parallel_mono_just_wrap_compute_ops                compute    35556.871 ops/s
// _001_parallel_mono_create_wrap_compute_ops           no_compute 28340756.409 ops/s
// _001_parallel_mono_create_wrap_compute_ops              compute    35437.397 ops/s
// _002_parallel_mono_defer_and_create_wrap_compute_ops no_compute 28889066.220 ops/s
// _002_parallel_mono_defer_and_create_wrap_compute_ops    compute    35747.654 ops/s
// _003_parallel_flux_create_wrap_compute_ops           no_compute  5185658.610 ops/s
// _003_parallel_flux_create_wrap_compute_ops              compute    34436.704 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(50_000)
class ReactorWrapComputeOps : ComputeOps, InvocationBenchmark {
  override val invocations: Int = 50_000

  @Param(value = ["no_compute", "compute"]) lateinit var computeMode: String

  override val benchmarkComputeMode: BenchmarkComputeMode by lazy {
    BenchmarkComputeMode.from(computeMode)
  }

  @Benchmark
  fun _000_parallel_mono_just_wrap_compute_ops() = parallelReactorMonoInvocationBenchmark {
    Mono.just(computeOps())
  }

  @Benchmark
  fun _001_parallel_mono_create_wrap_compute_ops() = parallelReactorMonoInvocationBenchmark {
    Mono.create { it.success(computeOps()) }
  }

  @Benchmark
  fun _002_parallel_mono_defer_and_create_wrap_compute_ops() =
    parallelReactorMonoInvocationBenchmark {
      Mono.defer { Mono.create { it.success(computeOps()) } }
    }

  @Benchmark
  fun _003_parallel_flux_create_wrap_compute_ops() = parallelReactorFluxInvocationBenchmark {
    Flux.create {
      it.next(computeOps())
      it.complete()
    }
  }
}
