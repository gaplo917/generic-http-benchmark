package io.github.gaplo917.idle

import io.github.gaplo917.helper.NoOps
import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineDispatcher
import org.openjdk.jmh.annotations.*

@State(Scope.Benchmark)
@Threads(1)
class CoroutineDispatcher : NoOps {
  lateinit var singleThreadContext: CoroutineDispatcher
  lateinit var twoThreadContext: CoroutineDispatcher

  @Setup
  fun setUp() {
    singleThreadContext = newSingleThreadContext("coroutine-single-thread")
    twoThreadContext = newFixedThreadPoolContext(2, "coroutine-single-thread")
  }

  @Benchmark
  fun _000_baseline() {
    noOps()
  }

  @Benchmark
  fun _001_coroutine() =
    runBlocking { noOps() }

  @Benchmark
  fun _002_coroutine_unconfined() =
    runBlocking(Dispatchers.Unconfined) { noOps() }

  @Benchmark
  fun _003_coroutine_default() =
    runBlocking(Dispatchers.Default) { noOps() }

  @Benchmark
  fun _004_coroutine_io() =
    runBlocking(Dispatchers.IO) { noOps() }

  @Benchmark
  fun _005_coroutine_single_thread_context() =
    runBlocking(singleThreadContext) { noOps() }

  @Benchmark
  fun _006_coroutine_two_thread_context() =
    runBlocking(twoThreadContext) { noOps() }
}
