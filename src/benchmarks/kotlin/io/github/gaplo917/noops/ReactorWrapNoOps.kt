package io.github.gaplo917.noops

import io.github.gaplo917.common.InvocationBenchmark
import io.github.gaplo917.noops.helper.NoOps
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


// _000_parallel_mono_just_wrap_no_ops               41648133.586 ops/s
// _001_parallel_mono_create_wrap_no_ops             28196450.886 ops/s
// _002_parallel_mono_defer_and_create_wrap_no_ops   25833117.908 ops/s
// _003_parallel_flux_create_wrap_no_ops              5458974.785 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(500_000)
class ReactorWrapNoOps : NoOps, InvocationBenchmark {
  override val invocations: Int = 500_000

  @Benchmark
  fun _000_parallel_mono_just_wrap_no_ops() = parallelReactorMonoInvocationBenchmark {
    Mono.just(noOps())
  }

  @Benchmark
  fun _001_parallel_mono_create_wrap_no_ops() = parallelReactorMonoInvocationBenchmark {
    Mono.create { it.success(noOps()) }
  }

  @Benchmark
  fun _002_parallel_mono_defer_and_create_wrap_no_ops() = parallelReactorMonoInvocationBenchmark {
    Mono.defer { Mono.create { it.success(noOps()) } }
  }

  @Benchmark
  fun _003_parallel_flux_create_wrap_no_ops() = parallelReactorFluxInvocationBenchmark {
    Flux.create {
      it.next(noOps())
      it.complete()
    }
  }
}
