package io.github.gaplo917

import java.time.Duration
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler

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

abstract class BenchmarkFoundation {
  abstract val mode: OpsMode
  abstract val ioDelay: Long

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

  fun syncTask(dependency: BenchmarkType? = null): BenchmarkType {
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

  suspend fun coroutineTaskSameScope(dependency: BenchmarkType? = null): BenchmarkType =
    coroutineScope {
      delay(ioDelay)
      when (dependency) {
        null -> computational(basicData)
        else -> computational(ops(basicData, dependency))
      }
    }

  suspend fun coroutineTaskWithContext(
    context: CoroutineContext,
    dependency: BenchmarkType? = null
  ): BenchmarkType =
    withContext(context) {
      delay(ioDelay)
      when (dependency) {
        null -> computational(basicData)
        else -> computational(ops(basicData, dependency))
      }
    }

  suspend fun coroutineTaskLaunchGlobalScopeSameContext(
    dependency: BenchmarkType? = null
  ): BenchmarkType = suspendCoroutine {
    GlobalScope.launch(it.context) {
      delay(ioDelay)
      when (dependency) {
        null -> it.resume(computational(basicData))
        else -> it.resume(computational(ops(basicData, dependency)))
      }
    }
  }

  suspend fun coroutineTaskLaunchGlobalScopeNewContext(
    context: CoroutineContext,
    dependency: BenchmarkType? = null
  ): BenchmarkType = suspendCoroutine {
    GlobalScope.launch(context) {
      delay(ioDelay)
      when (dependency) {
        null -> it.resume(computational(basicData))
        else -> it.resume(computational(ops(basicData, dependency)))
      }
    }
  }

  inline fun defaultReactorMonoTask(dependency: BenchmarkType? = null): Mono<BenchmarkType> {
    // we cannot use Mono.just(..) here because the `computational` function will be run immediately
    return Mono.create { observer ->
        when (dependency) {
          null -> observer.success(computational(basicData))
          else -> observer.success(computational(ops(basicData, dependency)))
        }
      }
      .delaySubscription(Duration.ofMillis(ioDelay))
  }
  inline fun customReactorMonoTask(
    dependency: BenchmarkType? = null,
    scheduler: Scheduler
  ): Mono<BenchmarkType> {
    // we cannot use Mono.just(..) here because the `computational` function will be run immediately
    return Mono.create { observer ->
        when (dependency) {
          null -> observer.success(computational(basicData))
          else -> observer.success(computational(ops(basicData, dependency)))
        }
      }
      .publishOn(scheduler)
      .subscribeOn(scheduler)
      .delaySubscription(Duration.ofMillis(ioDelay), scheduler)
  }
}
