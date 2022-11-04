package io.github.gaplo917.nonblocking

import io.github.gaplo917.helper.NonBlockingOps
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ScheduledExecutorService
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.Executors

@State(Scope.Benchmark)
@Threads(1)
@OperationsPerInvocation(50000)
class ReactorNonBlockingWrapper : NonBlockingOps {
  override lateinit var controlledExecutor: ScheduledExecutorService

  override val ioDelay: Long = 3

  override val invocations: Int = 50000

  @Setup
  fun setup() {
    controlledExecutor = Executors.newSingleThreadScheduledExecutor()
  }

  @TearDown
  fun tearDown() {
    controlledExecutor.shutdown()
  }

  @Benchmark
  fun _000_baseline() = parallelBaselineBenchmark()

  @Benchmark fun _001_mono_create() = parallelReactorBenchmark {
    Mono.create { nonBlockingOps { it.success(Unit) } }
  }

  @Benchmark
  fun _002_mono_defer_and_create() = parallelReactorBenchmark {
    Mono.defer { Mono.create { nonBlockingOps { it.success(Unit) } } }
  }

  @Benchmark
  fun _003_flux_create() = parallelReactorBenchmark {
    Flux.create {
      nonBlockingOps {
        it.next(Unit)
        it.complete()
      }
    }.last()
  }

}
