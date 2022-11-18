package io.github.jmh.compute.helper

import io.github.jmh.common.BenchmarkComputeMode
import io.github.jmh.common.DummyRequest
import io.github.jmh.common.DummyResponse

interface ComputeOps {
  val benchmarkComputeMode: BenchmarkComputeMode

  fun computeOps() {
    when (benchmarkComputeMode) {
      BenchmarkComputeMode.COMPUTE -> {
        // simulate serialization before IO task
        DummyRequest.dummy().toJson()

        // simulate serialization after IO task (returning list of object)
        DummyResponse.fromJson(DummyResponse.dummyJson)
      }
      BenchmarkComputeMode.NO_COMPUTE -> {}
    }
  }
}
