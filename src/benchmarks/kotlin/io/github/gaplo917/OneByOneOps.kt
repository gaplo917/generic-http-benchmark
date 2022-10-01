package io.github.gaplo917

import java.util.concurrent.TimeUnit
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import reactor.core.publisher.Mono

@State(Scope.Benchmark)
@Threads(1)
@Warmup(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
class OneByOneOps : BenchmarkFoundation() {
  @Param(value = ["2", "4", "8", "16", "32"]) lateinit var n: String

  @Param(value = ["noOps", "ops"]) lateinit var opsModeParam: String

  override val ioDelay: Long = 0

  override lateinit var mode: OpsMode

  @Setup
  fun setup() {
    mode = OpsMode.from(opsModeParam)
  }

  @Benchmark
  fun _000_sync_one_by_one(bh: Blackhole) {
    bh.consume(handleNSyncTask { dep -> syncTask(dep) })
  }

  @Benchmark
  fun _001_reactor_one_by_one(bh: Blackhole) {
    bh.consume(handleNDependentMonoTask { dep -> defaultReactorMonoTask(dep) }.block()!!)
  }

  @Benchmark
  fun _010_coroutine_same_scope(bh: Blackhole) {
    bh.consume(runBlocking { handleNCoroutineTask { dep -> coroutineTaskSameScope(dep) } })
  }

  @Benchmark
  fun _011_coroutine_launch_scope_same_context(bh: Blackhole) {
    bh.consume(
      runBlocking { handleNCoroutineTask { dep -> coroutineTaskLaunchGlobalScopeSameContext(dep) } }
    )
  }

  @Benchmark
  fun _020_coroutine_with_empty_context(bh: Blackhole) {
    bh.consume(
      runBlocking {
        handleNCoroutineTask { dep -> coroutineTaskWithContext(EmptyCoroutineContext, dep) }
      }
    )
  }

  @Benchmark
  fun _021_coroutine_launch_scope_empty_context(bh: Blackhole) {
    bh.consume(
      runBlocking {
        handleNCoroutineTask { dep ->
          coroutineTaskLaunchGlobalScopeNewContext(EmptyCoroutineContext, dep)
        }
      }
    )
  }

  @Benchmark
  fun _030_coroutine_with_default_context(bh: Blackhole) {
    bh.consume(
      runBlocking {
        handleNCoroutineTask { dep -> coroutineTaskWithContext(Dispatchers.Default, dep) }
      }
    )
  }

  @Benchmark
  fun _031_coroutine_launch_scope_default_context(bh: Blackhole) {
    bh.consume(
      runBlocking {
        handleNCoroutineTask { dep ->
          coroutineTaskLaunchGlobalScopeNewContext(Dispatchers.Default, dep)
        }
      }
    )
  }

  @Benchmark
  fun _040_coroutine_with_io_context(bh: Blackhole) {
    bh.consume(
      runBlocking { handleNCoroutineTask { dep -> coroutineTaskWithContext(Dispatchers.IO, dep) } }
    )
  }

  @Benchmark
  fun _041_coroutine_launch_scope_io_context(bh: Blackhole) {
    bh.consume(
      runBlocking {
        handleNCoroutineTask { dep ->
          coroutineTaskLaunchGlobalScopeNewContext(Dispatchers.IO, dep)
        }
      }
    )
  }

  @Benchmark
  fun _050_coroutine_launch_scope_unconfined_context(bh: Blackhole) {
    bh.consume(
      runBlocking {
        handleNCoroutineTask { dep ->
          coroutineTaskLaunchGlobalScopeNewContext(Dispatchers.Unconfined, dep)
        }
      }
    )
  }

  @Benchmark
  fun _051_coroutine_with_unconfined_context(bh: Blackhole) {
    bh.consume(
      runBlocking {
        handleNCoroutineTask { dep -> coroutineTaskWithContext(Dispatchers.Unconfined, dep) }
      }
    )
  }

  private final inline fun handleNSyncTask(
    crossinline handler: (dependency: BenchmarkType?) -> BenchmarkType
  ): BenchmarkType {
    var result = handler(null)
    for (i in 2..n.toInt()) {
      result = handler(result)
    }
    return result
  }

  private final suspend inline fun handleNCoroutineTask(
    crossinline handler: suspend (dependency: BenchmarkType?) -> BenchmarkType
  ): BenchmarkType {
    var result = handler(null)
    for (i in 2..n.toInt()) {
      result = handler(result)
    }
    return result
  }

  private final inline fun handleNDependentMonoTask(
    crossinline handler: (dependency: BenchmarkType?) -> Mono<BenchmarkType>
  ): Mono<BenchmarkType> {
    var result = handler(null)
    for (i in 2..n.toInt()) {
      result = result.flatMap { dep -> handler(dep) }
    }
    return result
  }
}
