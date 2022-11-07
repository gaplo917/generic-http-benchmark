package io.github.gaplo917.realworld

import io.github.gaplo917.common.InvocationBenchmark
import io.github.gaplo917.nonblocking.helper.NonBlockingOps
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import reactor.core.publisher.Mono
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Serializable
data class Data(val someInt: Int, val someMap: Map<String, String>, val someSet: Set<String>) {
  /**
   * Give light CPU loading on the merge operation.
   *
   * P.S. Don't want the data size grow with the number of ops to keep the experiment control. use
   * map and set to keep the data size as constant in merge
   */
  fun merge(that: Data?): Data {
    if (that == null) {
      return this
    }
    return this.copy(
      someInt = someInt + that.someInt,
      someMap = someMap + that.someMap,
      someSet = someSet + that.someSet,
    )
  }
}

typealias BenchmarkType = Data

enum class OpsMode {
  OPS,
  NO_OPS;
  companion object {
    fun from(arg: String): OpsMode {
      return when (arg) {
        "noOps" -> NO_OPS
        "ops" -> OPS
        else -> throw RuntimeException("this should not happen")
      }
    }
  }
}

abstract class BenchmarkFoundation: NonBlockingOps, InvocationBenchmark {
  abstract val mode: OpsMode
  override lateinit var controlledExecutor: ScheduledExecutorService

  open fun setup() {
    controlledExecutor = Executors.newSingleThreadScheduledExecutor()
  }

  open fun tearDown() {
    controlledExecutor.shutdown()
  }

  val basicData: BenchmarkType
    get() =
      when (mode) {
        OpsMode.NO_OPS -> Data(someInt = 1, someMap = mapOf(), someSet = setOf())
        OpsMode.OPS ->
          Data(
            someInt = 1,
            someMap = mapOf(*Array(10) { "item-$it" to "7ab8785a-c71e-485d-953f-0b0629c1a625" }),
            someSet = Array(20) { "7ab8785a-c71e-485d-953f-0b0629c1a625" }.toSet()
          )
      }

  fun syncOps(dependency: BenchmarkType? = null): BenchmarkType {
    return when (dependency) {
      null -> computational(basicData)
      else -> computational(ops(basicData, dependency))
    }
  }

  inline fun ops(a: BenchmarkType, b: BenchmarkType): BenchmarkType {
    return when (mode) {
      OpsMode.NO_OPS -> a
      OpsMode.OPS -> a.merge(b)
    }
  }

  inline fun computational(v: BenchmarkType): BenchmarkType {
    return when (mode) {
      OpsMode.NO_OPS -> v
      OpsMode.OPS -> {
        Json.decodeFromString(
          BenchmarkType.serializer(),
          Json.encodeToString(BenchmarkType.serializer(), v)
        )
      }
    }
  }

  suspend fun coSameScopeAsyncCBTask(dependency: BenchmarkType? = null): BenchmarkType =
    coroutineScope {
      coAsyncCBTask(dependency)
    }

  suspend fun coNewContextAsyncCBTask(
    context: CoroutineContext,
    dependency: BenchmarkType? = null
  ): BenchmarkType = withContext(context) { coAsyncCBTask(dependency) }

  suspend fun coLaunchAsyncCBTask(
    context: CoroutineContext,
    dependency: BenchmarkType? = null
  ): BenchmarkType = suspendCoroutine { cont ->
    GlobalScope.launch(context) { asyncCBTask(dependency) { cont.resume(it) } }
  }

  suspend fun coAsyncCBTask(dependency: BenchmarkType? = null): BenchmarkType =
    suspendCoroutine { cont ->
      asyncCBTask(dependency) { cont.resume(it) }
    }

  inline fun customReactorMonoTask(dependency: BenchmarkType? = null): Mono<BenchmarkType> {
    // we cannot use Mono.just(..) here because the `computational` function will be run immediately
    return Mono.create { observer -> asyncCBTask(dependency) { observer.success(it) } }
  }

  inline fun asyncCBTask(
    dependency: BenchmarkType? = null,
    crossinline cb: (BenchmarkType) -> Unit
  ) {
    if(ioDelay == 0L) {
      cb(syncOps(dependency))
    } else {
      val runnable = Runnable {
        cb(syncOps(dependency))
      }
      controlledExecutor.schedule(runnable, ioDelay, TimeUnit.MILLISECONDS)
    }
  }

}
