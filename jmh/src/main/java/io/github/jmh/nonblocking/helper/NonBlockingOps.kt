package io.github.jmh.nonblocking.helper

import io.github.jmh.common.BenchmarkComputeMode
import io.github.jmh.common.DummyRequest
import io.github.jmh.common.DummyResponse
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

interface NonBlockingOps {
  val controlledExecutor: ScheduledExecutorService
  val ioDelay: Long
  val invocations: Int
  val benchmarkComputeMode: BenchmarkComputeMode

  fun nonBlockingCallback(cb: () -> Unit): ScheduledFuture<*> {
    return when (benchmarkComputeMode) {
      BenchmarkComputeMode.COMPUTE -> {
        // simulate serialization workload before IO task
        DummyRequest.dummy().toJson()

        controlledExecutor.schedule(
          {
            // simulate deserialization workload after IO task
            DummyResponse.fromJson(DummyResponse.dummyJson)
            cb()
          },
          ioDelay,
          TimeUnit.MILLISECONDS
        )
      }
      BenchmarkComputeMode.NO_COMPUTE -> {
        controlledExecutor.schedule({ cb() }, ioDelay, TimeUnit.MILLISECONDS)
      }
    }
  }
}
