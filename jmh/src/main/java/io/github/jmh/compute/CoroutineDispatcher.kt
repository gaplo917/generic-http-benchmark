package io.github.jmh.compute

import io.github.jmh.common.BenchmarkComputeMode
import io.github.jmh.common.DispatcherParameterConversion
import io.github.jmh.common.InvocationBenchmark
import io.github.jmh.compute.helper.ComputeOps
import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineDispatcher
import org.openjdk.jmh.annotations.*

//_001_parallel_coroutine_compute_ops  no_compute    Unconfined  21869942.222 ±  419615.298  ops/s
//_001_parallel_coroutine_compute_ops  no_compute            IO    129187.312 ±   64643.324  ops/s
//_001_parallel_coroutine_compute_ops  no_compute       Default   1035776.396 ±  205297.372  ops/s
//_001_parallel_coroutine_compute_ops  no_compute  SingleThread   3139263.112 ±  384889.174  ops/s
//_001_parallel_coroutine_compute_ops  no_compute     TwoThread   4093085.972 ±   47958.594  ops/s
//_001_parallel_coroutine_compute_ops     compute    Unconfined     35779.858 ±     621.517  ops/s
//_001_parallel_coroutine_compute_ops     compute            IO    243855.395 ±   19404.733  ops/s
//_001_parallel_coroutine_compute_ops     compute       Default    260034.740 ±    9475.885  ops/s
//_001_parallel_coroutine_compute_ops     compute  SingleThread     33886.504 ±    1611.770  ops/s
//_001_parallel_coroutine_compute_ops     compute     TwoThread     66728.191 ±     986.027  ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(50_000)
class CoroutineDispatcher : ComputeOps, InvocationBenchmark, DispatcherParameterConversion {
    override val invocations: Int = 50_000

    @Param(value = ["no_compute", "compute"])
    lateinit var computeMode: String

    override val benchmarkComputeMode: BenchmarkComputeMode by lazy {
        BenchmarkComputeMode.from(computeMode)
    }

    @Param(value = ["Unconfined", "IO", "Default", "SingleThread", "TwoThread"])
    lateinit var dispatcher: String

    lateinit var coroutineDispatcher: CoroutineDispatcher

    @Setup
    fun setUp() {
        coroutineDispatcher = convertDispatcherParam(dispatcher)
    }

    @Benchmark
    fun _001_parallel_coroutine_compute_ops() =
        parallelCoroutineInvocationBenchmark(coroutineDispatcher) { computeOps() }
}
