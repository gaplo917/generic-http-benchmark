package io.github.gaplo917

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers

@State(Scope.Benchmark)
@Threads(1)
@Warmup(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
class ParallelReactorOps : BenchmarkFoundation() {
  @Param(
    value =
      [
        "1024",
        "2048",
      ]
  )
  lateinit var n: String

  @Param(
    value =
      [
        "1",
        "2",
        "4",
        "8",
      ]
  )
  lateinit var threads: String

  @Param(value = ["noOps", "ops"]) lateinit var opsModeParam: String

  override val ioDelay: Long = 5L

  override lateinit var mode: OpsMode

  lateinit var customExecutor: ExecutorService

  lateinit var customScheduler: Scheduler

  @Setup
  fun setup() {
    mode = OpsMode.from(opsModeParam)
    customExecutor = Executors.newScheduledThreadPool(threads.toInt())
    customScheduler = Schedulers.fromExecutorService(customExecutor)
  }

  @TearDown
  fun tearDown() {
    customExecutor.shutdown()
  }

  @Benchmark
  fun _200_mono_zip(bh: Blackhole) {
    bh.consume(monoZip(n.toInt()).block()!!)
  }

  @Benchmark
  fun _300_mono_flux_merge_reduce(bh: Blackhole) {
    bh.consume(fluxMergeReduce(n.toInt()).block()!!)
  }

  private fun monoZip(size: Int): Mono<BenchmarkType> {
    if (size <= 0) {
      throw RuntimeException("not expected size <= 0")
    }

    return Mono.zip(
        {
          val iter = it.iterator()
          var acc = iter.next() as BenchmarkType

          while (iter.hasNext()) {
            acc = ops(acc, iter.next() as BenchmarkType)
          }
          acc
        },
        *Array(size) { customReactorMonoTask(scheduler = customScheduler) }
      )
      .publishOn(customScheduler)
      .subscribeOn(customScheduler)
  }

  private fun fluxMergeReduce(size: Int): Mono<BenchmarkType> {
    if (size <= 0) {
      throw RuntimeException("not expected size <= 0")
    }

    return Flux.merge(*Array(size) { customReactorMonoTask(scheduler = customScheduler) })
      .publishOn(customScheduler)
      .subscribeOn(customScheduler)
      .reduce(::ops)
  }
}
