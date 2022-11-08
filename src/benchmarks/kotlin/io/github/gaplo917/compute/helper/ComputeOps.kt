package io.github.gaplo917.compute.helper

import io.github.gaplo917.common.BenchmarkComputeMode
import io.github.gaplo917.common.DummyRequest
import io.github.gaplo917.common.DummyResponse

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
