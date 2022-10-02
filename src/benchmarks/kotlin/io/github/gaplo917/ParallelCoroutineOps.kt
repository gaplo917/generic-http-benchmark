package io.github.gaplo917

import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

@State(Scope.Benchmark)
@Threads(1)
@Warmup(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
class ParallelCoroutineOps : BenchmarkFoundation() {
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

  @Param(value = ["noOps", "ops"]) lateinit var opsMode: String

  private val delayDistribution = Array(1024) { index -> ((index + 256) * 100 / 256) / 100L }

  @Volatile private var count = 0

  override val ioDelay: Long
    get() {
      return delayDistribution[count++ % delayDistribution.size]
    }

  override lateinit var mode: OpsMode

  lateinit var customCoroutineContext: CoroutineContext

  @Setup
  fun setup() {
    mode = OpsMode.from(opsMode)
    customCoroutineContext = newFixedThreadPoolContext(threads.toInt(), "coroutine-thread-pool")
  }

  @Benchmark
  fun _100_coroutine_same_scope(bh: Blackhole) {
    bh.consume(runBlocking { coroutineSameScopeImperative(n.toInt()) })
  }

  @Benchmark
  fun _110_coroutine_launch_scope(bh: Blackhole) {
    bh.consume(runBlocking { coroutineLaunchScopeImperative(n.toInt()) })
  }

  private suspend fun coroutineSameScopeImperative(size: Int): BenchmarkType {
    if (size <= 0) {
      throw RuntimeException("not expected size <= 0")
    }

    return coroutineScope {
      val iter =
        Array(size) { async(customCoroutineContext) { coroutineTaskSameScope() } }.iterator()

      var acc = iter.next().await()

      while (iter.hasNext()) {
        acc = ops(acc, iter.next().await())
      }
      acc
    }
  }

  private suspend fun coroutineLaunchScopeImperative(size: Int): BenchmarkType {
    if (size <= 0) {
      throw RuntimeException("not expected size <= 0")
    }

    return coroutineScope {
      val iter =
        Array(size) {
            async(customCoroutineContext) { coroutineTaskLaunchGlobalScopeSameContext() }
          }
          .iterator()

      var acc = iter.next().await()

      while (iter.hasNext()) {
        acc = ops(acc, iter.next().await())
      }
      acc
    }
  }
}
