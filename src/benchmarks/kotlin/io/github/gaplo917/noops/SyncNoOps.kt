package io.github.gaplo917.noops

import io.github.gaplo917.common.InvocationBenchmark
import io.github.gaplo917.noops.helper.NoOps
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*

// _000_sync_raw_no_ops 145149515.450 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(500_000)
class SyncNoOps : NoOps, InvocationBenchmark {
  override val invocations: Int = 500_000

  @Benchmark fun _000_sync_raw_no_ops() = syncInvocationBenchmark { noOps() }
}
