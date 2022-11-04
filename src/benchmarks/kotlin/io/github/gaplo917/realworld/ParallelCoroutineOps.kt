package io.github.gaplo917.realworld

import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*

@State(Scope.Benchmark)
@Threads(1)
@OperationsPerInvocation(50000)
class ParallelCoroutineOps : BenchmarkFoundation() {
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

  private val delayDistribution = listOf(1, 2, 3)

  private var count = 0

  override val ioDelay: Long
    get() {
      return (delayDistribution[count++ % delayDistribution.size]).toLong()
    }

  override lateinit var mode: OpsMode

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
  fun _100_coroutine_imperative() = parallelCoBenchmark { coroutineImperative(n.toInt()) }

  @Benchmark
  fun _110_coroutine_declarative() = parallelCoBenchmark { coroutineDeclarative(n.toInt()) }

  private suspend fun coroutineDeclarative(size: Int): BenchmarkType {
    return coroutineScope { Array(size) { async(Dispatchers.Unconfined) { coAsyncCBTask() } } }
      .map { it.await() }
      .reduce { acc, data -> ops(acc, data) }
  }

  private suspend fun coroutineImperative(size: Int): BenchmarkType {
    return coroutineScope {
      val iter = Array(size) { async(Dispatchers.Unconfined) { coAsyncCBTask() } }.iterator()

      var acc = iter.next().await()

      while (iter.hasNext()) {
        acc = ops(acc, iter.next().await())
      }
      acc
    }
  }
}
