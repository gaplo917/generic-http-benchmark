package io.github.gaplo917.noops

import io.github.gaplo917.common.InvocationBenchmark
import io.github.gaplo917.noops.helper.NoOps
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers

//  _001_parallel_mono_just_subscribe_on_default_scheduler         41842709.719 ops/s
//  _002_parallel_mono_just_subscribe_on_immediate_scheduler       12617220.425 ops/s
//  _003_parallel_mono_just_subscribe_on_parallel_scheduler         1249029.702 ops/s
//  _004_parallel_mono_just_subscribe_on_single_scheduler           3526747.808 ops/s
//  _005_parallel_mono_just_subscribe_on_boundedElastic_scheduler    556959.878 ops/s
//  _006_parallel_mono_just_subscribe_on_single_thread_scheduler    7012093.075 ops/s
//  _007_parallel_mono_just_subscribe_on_two_thread_scheduler       7185448.800 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(500_000)
class ReactorScheduler : NoOps, InvocationBenchmark {
  override val invocations: Int = 500_000
  lateinit var singleThreadExecutorService: ExecutorService
  lateinit var twoThreadExecutorService: ExecutorService
  lateinit var singleThreadScheduler: Scheduler
  lateinit var twoThreadScheduler: Scheduler

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
      Mono.just(noOps())
    }

  @Benchmark
  fun _002_parallel_mono_just_subscribe_on_immediate_scheduler() =
    parallelReactorMonoInvocationBenchmark {
      Mono.just(noOps()).subscribeOn(Schedulers.immediate())
    }

  @Benchmark
  fun _003_parallel_mono_just_subscribe_on_parallel_scheduler() =
    parallelReactorMonoInvocationBenchmark {
      Mono.just(noOps()).subscribeOn(Schedulers.parallel())
    }

  @Benchmark
  fun _004_parallel_mono_just_subscribe_on_single_scheduler() =
    parallelReactorMonoInvocationBenchmark {
      Mono.just(noOps()).subscribeOn(Schedulers.single())
    }

  @Benchmark
  fun _005_parallel_mono_just_subscribe_on_boundedElastic_scheduler() =
    parallelReactorMonoInvocationBenchmark {
      Mono.just(noOps()).subscribeOn(Schedulers.boundedElastic())
    }

  @Benchmark
  fun _006_parallel_mono_just_subscribe_on_single_thread_scheduler() =
    parallelReactorMonoInvocationBenchmark {
      Mono.just(noOps()).subscribeOn(singleThreadScheduler)
    }

  @Benchmark
  fun _007_parallel_mono_just_subscribe_on_two_thread_scheduler() =
    parallelReactorMonoInvocationBenchmark {
      Mono.just(noOps()).subscribeOn(twoThreadScheduler)
    }
}
