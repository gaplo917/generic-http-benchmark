package io.github.gaplo917.nonblocking.helper

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

interface NonBlockingOps {
  val controlledExecutor: ScheduledExecutorService
  val ioDelay: Long
  val invocations: Int

  fun nonBlockingCallback(cb: () -> Unit) {
    controlledExecutor.schedule({ cb() }, ioDelay, TimeUnit.MILLISECONDS)
  }
}
