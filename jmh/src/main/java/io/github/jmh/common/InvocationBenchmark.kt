package io.github.jmh.common

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import kotlinx.coroutines.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

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

  fun sequentialFutureInvocationBenchmark(task: () -> CompletableFuture<*>) {
    val latch = CountDownLatch(invocations)
    var future: CompletableFuture<*>? = null
    for (i in 1..invocations) {
      future =
        if (future == null) {
          task().thenAcceptAsync { latch.countDown() }
        } else {
          future.thenComposeAsync { task().thenAcceptAsync { latch.countDown() } }
        }
    }
    latch.await()
  }

  fun sequentialReactorMonoInvocationBenchmark(task: () -> Mono<*>) {
    val latch = CountDownLatch(invocations)
    var mono: Mono<*>? = null
    for (i in 1..invocations) {
      mono =
        if (mono == null) {
          task().doOnNext { latch.countDown() }
        } else {
          mono.flatMap { task().doOnNext { latch.countDown() } }
        }
    }
    mono?.subscribe {}
    latch.await()
  }

  fun sequentialReactorFluxInvocationBenchmark(task: () -> Flux<*>) {
    val latch = CountDownLatch(invocations)
    var flux: Flux<*>? = null
    for (i in 1..invocations) {
      flux =
        if (flux == null) {
          task().doOnComplete { latch.countDown() }
        } else {
          flux.flatMap { task().doOnComplete { latch.countDown() } }
        }
    }
    flux?.subscribe()
    latch.await()
  }

  fun <T> sequentialCoroutineInvocationBenchmark(
    context: CoroutineDispatcher = Dispatchers.Unconfined,
    task: suspend () -> T
  ) {
    val latch = CountDownLatch(invocations)

    GlobalScope.launch(context) {
      for (i in 1..invocations) {
        task()
        latch.countDown()
      }
    }

    latch.await()
  }

  fun <T> sequentialCallbackInvocationBenchmark(cb: (() -> Unit) -> T) {
    val latch = CountDownLatch(invocations)

    for (i in 1..invocations) {
      val secondLatch = CountDownLatch(1)
      cb {
        secondLatch.countDown()
        latch.countDown()
      }
      secondLatch.await()
    }

    latch.await()
  }

  fun <T> parallelCallbackInvocationBenchmark(cb: (() -> Unit) -> T) {
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

  fun <T> parallelCoroutineInvocationBenchmark(
    context: CoroutineDispatcher = Dispatchers.Unconfined,
    task: suspend () -> T
  ) {
    val latch = CountDownLatch(invocations)

    for (i in 1..invocations) {
      GlobalScope.launch(context) {
        task()
        latch.countDown()
      }
    }

    latch.await()
  }
}
