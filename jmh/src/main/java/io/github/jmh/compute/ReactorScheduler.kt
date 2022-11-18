package io.github.jmh.compute

import io.github.jmh.common.BenchmarkComputeMode
import io.github.jmh.common.InvocationBenchmark
import io.github.jmh.compute.helper.ComputeOps
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers

// _001_parallel_mono_just_subscribe_on_default_scheduler         no_compute 27716661.617 ops/s
// _001_parallel_mono_just_subscribe_on_default_scheduler            compute    35129.753 ops/s
// _002_parallel_mono_just_subscribe_on_immediate_scheduler       no_compute 18044845.570 ops/s
// _002_parallel_mono_just_subscribe_on_immediate_scheduler          compute    34257.860 ops/s
// _003_parallel_mono_just_subscribe_on_parallel_scheduler        no_compute  1129395.593 ops/s
// _003_parallel_mono_just_subscribe_on_parallel_scheduler           compute   232298.954 ops/s
// _004_parallel_mono_just_subscribe_on_single_scheduler          no_compute  2464327.433 ops/s
// _004_parallel_mono_just_subscribe_on_single_scheduler             compute    34620.035 ops/s
// _005_parallel_mono_just_subscribe_on_boundedElastic_scheduler  no_compute   379698.786 ops/s
// _005_parallel_mono_just_subscribe_on_boundedElastic_scheduler     compute   186950.163 ops/s
// _006_parallel_mono_just_subscribe_on_single_thread_scheduler   no_compute  5585158.704 ops/s
// _006_parallel_mono_just_subscribe_on_single_thread_scheduler      compute    34132.306 ops/s
// _007_parallel_mono_just_subscribe_on_two_thread_scheduler      no_compute  4728782.173 ops/s
// _007_parallel_mono_just_subscribe_on_two_thread_scheduler         compute    64305.565 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(50_000)
class ReactorScheduler : ComputeOps, InvocationBenchmark {
  override val invocations: Int = 50_000
  lateinit var singleThreadExecutorService: ExecutorService
  lateinit var twoThreadExecutorService: ExecutorService
  lateinit var singleThreadScheduler: Scheduler
  lateinit var twoThreadScheduler: Scheduler

  @Param(value = ["no_compute", "compute"]) lateinit var computeMode: String

  override val benchmarkComputeMode: BenchmarkComputeMode by lazy {
    BenchmarkComputeMode.from(computeMode)
  }

  @Setup
  fun setUp() {
    singleThreadExecutorService = Executors.newSingleThreadExecutor()
    twoThreadExecutorService = Executors.newFixedThreadPool(2)
    singleThreadScheduler = Schedulers.fromExecutorService(singleThreadExecutorService)
    twoThreadScheduler = Schedulers.fromExecutorService(twoThreadExecutorService)
  }

  @TearDown
  fun tearDown() {
    singleThreadExecutorService.shutdown()
    twoThreadExecutorService.shutdown()
  }

  @Benchmark
  fun _001_parallel_mono_just_subscribe_on_default_scheduler() =
    parallelReactorMonoInvocationBenchmark {
      Mono.create { observer -> observer.success(computeOps()) }
    }

  @Benchmark
  fun _002_parallel_mono_just_subscribe_on_immediate_scheduler() =
    parallelReactorMonoInvocationBenchmark {
      Mono.create { observer -> observer.success(computeOps()) }.subscribeOn(Schedulers.immediate())
    }

  @Benchmark
  fun _003_parallel_mono_just_subscribe_on_parallel_scheduler() =
    parallelReactorMonoInvocationBenchmark {
      Mono.create { observer -> observer.success(computeOps()) }.subscribeOn(Schedulers.parallel())
    }

  @Benchmark
  fun _004_parallel_mono_just_subscribe_on_single_scheduler() =
    parallelReactorMonoInvocationBenchmark {
      Mono.create { observer -> observer.success(computeOps()) }.subscribeOn(Schedulers.single())
    }

  @Benchmark
  fun _005_parallel_mono_just_subscribe_on_boundedElastic_scheduler() =
    parallelReactorMonoInvocationBenchmark {
      Mono.create { observer -> observer.success(computeOps()) }
        .subscribeOn(Schedulers.boundedElastic())
    }

  @Benchmark
  fun _006_parallel_mono_just_subscribe_on_single_thread_scheduler() =
    parallelReactorMonoInvocationBenchmark {
      Mono.create { observer -> observer.success(computeOps()) }.subscribeOn(singleThreadScheduler)
    }

  @Benchmark
  fun _007_parallel_mono_just_subscribe_on_two_thread_scheduler() =
    parallelReactorMonoInvocationBenchmark {
      Mono.create { observer -> observer.success(computeOps()) }.subscribeOn(twoThreadScheduler)
    }
}
