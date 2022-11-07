package io.github.gaplo917.common

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import kotlinx.coroutines.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/** Implement a control for blocking and non-blocking invocation benchmark */
interface InvocationBenchmark {
  val invocations: Int

  fun syncInvocationBenchmark(blocking: () -> Unit) {
    val latch = CountDownLatch(invocations)

    for (i in 1..invocations) {
      blocking()
      latch.countDown()
    }

    latch.await()
  }

  fun sequentialCoroutineInvocationBenchmark(
    context: CoroutineDispatcher = Dispatchers.Unconfined,
    cb: suspend () -> Unit
  ) {
    val latch = CountDownLatch(invocations)

    GlobalScope.launch(context) {
      for (i in 1..invocations) {
        cb()
        latch.countDown()
      }
    }

    latch.await()
  }

  fun parallelCallbackInvocationBenchmark(cb: (() -> Unit) -> Unit) {
    val latch = CountDownLatch(invocations)

    for (i in 1..invocations) {
      cb { latch.countDown() }
    }

    latch.await()
  }

  fun parallelFutureInvocationBenchmark(task: () -> CompletableFuture<*>) {
    val latch = CountDownLatch(invocations)
    for (i in 1..invocations) {
      task().thenAcceptAsync { latch.countDown() }
    }
    latch.await()
  }

  fun parallelReactorMonoInvocationBenchmark(task: () -> Mono<*>) {
    val latch = CountDownLatch(invocations)
    for (i in 1..invocations) {
      task().subscribe { latch.countDown() }
    }
    latch.await()
  }

  fun parallelReactorFluxInvocationBenchmark(task: () -> Flux<*>) {
    val latch = CountDownLatch(invocations)
    for (i in 1..invocations) {
      task().doOnComplete { latch.countDown() }.subscribe()
    }
    latch.await()
  }

  fun parallelCoroutineInvocationBenchmark(
    context: CoroutineDispatcher = Dispatchers.Unconfined,
    cb: suspend () -> Unit
  ) {
    val latch = CountDownLatch(invocations)

    for (i in 1..invocations) {
      GlobalScope.launch(context) {
        cb()
        latch.countDown()
      }
    }

    latch.await()
  }
}
