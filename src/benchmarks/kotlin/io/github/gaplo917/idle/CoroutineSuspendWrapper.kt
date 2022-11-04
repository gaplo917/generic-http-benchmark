package io.github.gaplo917.idle

import io.github.gaplo917.helper.NoOps
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*

@State(Scope.Benchmark)
@Threads(1)
class CoroutineSuspendWrapper: NoOps {

  @Benchmark fun _000_baseline() = runBlocking(Dispatchers.Unconfined) { noOps() }

  @Benchmark fun _001_suspend() = runBlocking(Dispatchers.Unconfined) { suspendNoOps() }

  @Benchmark fun _002_suspendCancellable() = runBlocking(Dispatchers.Unconfined) { suspendCancellableNoOps() }

  @Benchmark fun _003_suspendLaunch() = runBlocking(Dispatchers.Unconfined) { suspendLaunchNoOps() }

  @Benchmark
  fun _004_coroutine_scope() = runBlocking(Dispatchers.Unconfined) { coroutineScopeNoOps() }

  @Benchmark
  fun _005_supervisor_scope() =
    runBlocking(Dispatchers.Unconfined) { supervisorScopeScopeNoOps() }

  @Benchmark
  fun _006_with_context() =
    runBlocking(Dispatchers.Unconfined) { withContextNoOps() }

  suspend fun suspendNoOps() = suspendCoroutine { it.resume(noOps()) }

  suspend fun suspendCancellableNoOps() = suspendCancellableCoroutine { it.resume(noOps()) }

  suspend fun coroutineScopeNoOps() = coroutineScope { noOps() }

  suspend fun supervisorScopeScopeNoOps() = supervisorScope { noOps() }

  suspend fun withContextNoOps() = withContext(Dispatchers.Unconfined) {
    noOps()
  }

  suspend fun suspendLaunchNoOps() = suspendCoroutine {
    GlobalScope.launch(Dispatchers.Unconfined) {
      it.resume(noOps())
    }
  }
}
