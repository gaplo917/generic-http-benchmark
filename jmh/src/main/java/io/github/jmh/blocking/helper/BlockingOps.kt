package io.github.jmh.blocking.helper

import io.github.jmh.common.BenchmarkComputeMode
import io.github.jmh.common.DummyRequest
import io.github.jmh.common.DummyResponse

interface BlockingOps {
  val ioDelay: Long
  val benchmarkComputeMode: BenchmarkComputeMode

  fun blockingIO() {
    when (benchmarkComputeMode) {
      BenchmarkComputeMode.COMPUTE -> {
        // simulate serialization before IO task
        DummyRequest.dummy().toJson()

        Thread.sleep(ioDelay)

        // simulate serialization after IO task (returning list of object)
        DummyResponse.fromJson(DummyResponse.dummyJson)
      }
      BenchmarkComputeMode.NO_COMPUTE -> {
        Thread.sleep(ioDelay)
      }
    }
  }
}
