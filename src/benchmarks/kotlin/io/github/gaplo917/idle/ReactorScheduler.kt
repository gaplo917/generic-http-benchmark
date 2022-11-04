package io.github.gaplo917.idle

import io.github.gaplo917.helper.NoOps
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@State(Scope.Benchmark)
@Threads(1)
class ReactorScheduler : NoOps {
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
  fun _000_baseline() {
    noOps()
  }

  @Benchmark
  fun _001_reactor() = Mono.just(noOps()).block()

  @Benchmark
  fun _002_reactor_immediate() = Mono.just(noOps()).subscribeOn(Schedulers.immediate()).block()

  @Benchmark
  fun _003_reactor_parallel() = Mono.just(noOps()).subscribeOn(Schedulers.parallel()).block()

  @Benchmark
  fun _004_reactor_single() = Mono.just(noOps()).subscribeOn(Schedulers.single()).block()

  @Benchmark
  fun _005_reactor_boundedElastic() = Mono.just(noOps()).subscribeOn(Schedulers.boundedElastic()).block()

  @Benchmark
  fun _006_reactor_single_thread_scheduler() = Mono.just(noOps()).subscribeOn(singleThreadScheduler).block()

  @Benchmark
  fun _007_reactor_two_thread_scheduler() = Mono.just(noOps()).subscribeOn(twoThreadScheduler).block()


}
