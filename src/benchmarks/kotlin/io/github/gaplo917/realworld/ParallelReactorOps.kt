package io.github.gaplo917.realworld

import org.openjdk.jmh.annotations.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler

@State(Scope.Benchmark)
@Threads(1)
@OperationsPerInvocation(50000)
class ParallelReactorOps : BenchmarkFoundation() {
  @Param(
    value =
      [
        "8",
        "16",
      ]
  )
  lateinit var n: String
  
  override val invocations: Int = 50000

  @Param(value = ["noOps", "ops"]) lateinit var opsModeParam: String

  private val delayDistribution = listOf(1,2,3)

  private var count = 0

  override val ioDelay: Long
    get() {
      return delayDistribution[count++ % delayDistribution.size].toLong()
    }

  override lateinit var mode: OpsMode

  lateinit var customScheduler: Scheduler

  @Setup
  override fun setup() {
    super.setup()
    mode = OpsMode.from(opsModeParam)
  }

  @TearDown
  override fun tearDown() {
    super.tearDown()
  }

  @Benchmark
  fun _200_mono_zip() = parallelReactorBenchmark {
    monoZip(n.toInt())
  }

  @Benchmark
  fun _300_mono_flux_merge_reduce() = parallelReactorBenchmark {
    fluxMergeReduce(n.toInt())
  }

  private fun monoZip(size: Int): Mono<BenchmarkType> {
    return Mono.zip(
        {
          val iter = it.iterator()
          var acc = iter.next() as BenchmarkType

          while (iter.hasNext()) {
            acc = ops(acc, iter.next() as BenchmarkType)
          }
          acc
        },
        *Array(size) { customReactorMonoTask() }
      )
//      .publishOn(Schedulers.immediate())
  }

  private fun fluxMergeReduce(size: Int): Mono<BenchmarkType> {
    return Flux.merge(*Array(size) { customReactorMonoTask() })
      .parallel()
      .reduce(::ops)
  }
}
