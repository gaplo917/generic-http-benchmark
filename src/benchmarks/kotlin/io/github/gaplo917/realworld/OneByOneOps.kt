package io.github.gaplo917.realworld

import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import reactor.core.publisher.Mono

@State(Scope.Benchmark)
@Threads(1)
class OneByOneOps : BenchmarkFoundation() {
  @Param(
    value =
      [
        "1",
        "2",
        "4",
        "8",
        "16",
      ]
  )
  lateinit var n: String
  override val invocations: Int = 50000

  @Param(value = ["noOps", "ops"]) lateinit var opsModeParam: String

  override val ioDelay: Long = 0

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
  fun _000_sync_one_by_one(bh: Blackhole) {
    handleNSyncTask { dep -> syncOps(dep) }
  }

  @Benchmark
  fun _001_reactor_one_by_one(bh: Blackhole) {
    handleNDependentMonoTask { dep -> customReactorMonoTask(dep) }.block()
  }

  @Benchmark
  fun _010_coroutine(bh: Blackhole) =
    runBlocking(Dispatchers.Unconfined) { handleNCoroutineTask { dep -> coAsyncCBTask(dep) } }

  @Benchmark
  fun _010_coroutine_same_scope(bh: Blackhole) =
    runBlocking(Dispatchers.Unconfined) {
      handleNCoroutineTask { dep -> coSameScopeAsyncCBTask(dep) }
    }

  @Benchmark
  fun _020_coroutine_with_empty_context(bh: Blackhole) =
    runBlocking(Dispatchers.Unconfined) {
      handleNCoroutineTask { dep -> coNewContextAsyncCBTask(EmptyCoroutineContext, dep) }
    }

  @Benchmark
  fun _021_coroutine_launch_empty_context(bh: Blackhole) =
    runBlocking(Dispatchers.Unconfined) {
      handleNCoroutineTask { dep -> coLaunchAsyncCBTask(EmptyCoroutineContext, dep) }
    }

  @Benchmark
  fun _030_coroutine_with_default_context(bh: Blackhole) =
    runBlocking(Dispatchers.Unconfined) {
      handleNCoroutineTask { dep -> coNewContextAsyncCBTask(Dispatchers.Default, dep) }
    }

  @Benchmark
  fun _031_coroutine_launch_default_context(bh: Blackhole) =
    runBlocking(Dispatchers.Unconfined) {
      handleNCoroutineTask { dep -> coLaunchAsyncCBTask(Dispatchers.Default, dep) }
    }

  @Benchmark
  fun _040_coroutine_with_io_context(bh: Blackhole) =
    runBlocking(Dispatchers.Unconfined) {
      handleNCoroutineTask { dep -> coNewContextAsyncCBTask(Dispatchers.IO, dep) }
    }

  @Benchmark
  fun _041_coroutine_launch_io_context(bh: Blackhole) =
    runBlocking(Dispatchers.Unconfined) {
      handleNCoroutineTask { dep -> coLaunchAsyncCBTask(Dispatchers.IO, dep) }
    }

  @Benchmark
  fun _050_coroutine_launch_unconfined_context(bh: Blackhole) =
    runBlocking(Dispatchers.Unconfined) {
      handleNCoroutineTask { dep -> coLaunchAsyncCBTask(Dispatchers.Unconfined, dep) }
    }

  @Benchmark
  fun _051_coroutine_with_unconfined_context(bh: Blackhole) =
    runBlocking(Dispatchers.Unconfined) {
      handleNCoroutineTask { dep -> coNewContextAsyncCBTask(Dispatchers.Unconfined, dep) }
    }

  private final inline fun handleNSyncTask(
    crossinline handler: (dependency: BenchmarkType?) -> BenchmarkType
  ): BenchmarkType {
    var result = handler(null)
    for (i in 2..n.toInt()) {
      result = handler(ops(result, result))
    }
    return result
  }

  private final suspend inline fun handleNCoroutineTask(
    crossinline handler: suspend (dependency: BenchmarkType?) -> BenchmarkType
  ): BenchmarkType {
    var result = handler(null)
    for (i in 2..n.toInt()) {
      result = handler(ops(result, result))
    }
    return result
  }

  private final inline fun handleNDependentMonoTask(
    crossinline handler: (dependency: BenchmarkType?) -> Mono<BenchmarkType>
  ): Mono<BenchmarkType> {
    var result = handler(null)
    for (i in 2..n.toInt()) {
      result = result.map { ops(it, it) }.flatMap { dep -> handler(dep) }
    }
    return result
  }
}
