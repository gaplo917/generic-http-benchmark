package io.github.gaplo917.helper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import reactor.core.publisher.Mono
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

interface NonBlockingOps {
  val controlledExecutor: ScheduledExecutorService
  val ioDelay: Long
  val invocations: Int

  fun nonBlockingOps(cb: () -> Unit) {
    controlledExecutor.schedule({ cb() }, ioDelay, TimeUnit.MILLISECONDS)
  }

  fun parallelBaselineBenchmark() {
    val latch = CountDownLatch(invocations)
    for (i in 1..invocations) {
      nonBlockingOps { latch.countDown() }
    }
    latch.await()
  }

  fun parallelReactorBenchmark(task: () -> Mono<*>) {
    val latch = CountDownLatch(invocations)

    for (i in 1..invocations) {
      task().subscribe {
        latch.countDown()
      }
    }

    latch.await()
  }

  fun parallelCoBenchmark(cb: suspend () -> Unit) {
    val latch = CountDownLatch(invocations)

    for (i in 1..invocations) {
      GlobalScope.launch(Dispatchers.Unconfined) {
        cb()
        latch.countDown()
      }
    }

    latch.await()
  }
}
