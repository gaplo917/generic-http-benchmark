package io.github.gaplo917.nonblocking

import io.github.gaplo917.common.InvocationBenchmark
import io.github.gaplo917.nonblocking.helper.NonBlockingOps
import io.netty.channel.nio.NioEventLoopGroup
import java.util.concurrent.ScheduledExecutorService
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

//
// _000_parallel_no_wrap_callback                    3008131.465 ops/s
// _001_parallel_mono_create_wrap_callback           3203924.399 ops/s
// _002_parallel_mono_defer_and_create_wrap_callback 3045708.922 ops/s
// _003_parallel_flux_create_wrap_callback           2662122.935 ops/s
@State(Scope.Benchmark)
@OperationsPerInvocation(500_000)
class ReactorWrapNonBlockingIO : NonBlockingOps, InvocationBenchmark {
  override lateinit var controlledExecutor: ScheduledExecutorService

  override val ioDelay: Long = 3

  override val invocations: Int = 500_000

  @Setup
  fun setup() {
    controlledExecutor = NioEventLoopGroup()
  }

  @TearDown
  fun tearDown() {
    controlledExecutor.shutdown()
  }

  @Benchmark
  fun _000_parallel_no_wrap_callback() = parallelCallbackInvocationBenchmark(::nonBlockingCallback)

  @Benchmark
  fun _001_parallel_mono_create_wrap_callback() = parallelReactorMonoInvocationBenchmark {
    Mono.create { nonBlockingCallback { it.success(Unit) } }
  }

  @Benchmark
  fun _002_parallel_mono_defer_and_create_wrap_callback() = parallelReactorMonoInvocationBenchmark {
    Mono.defer { Mono.create { nonBlockingCallback { it.success(Unit) } } }
  }

  @Benchmark
  fun _003_parallel_flux_create_wrap_callback() = parallelReactorMonoInvocationBenchmark {
    Flux.create {
        nonBlockingCallback {
          it.next(Unit)
          it.complete()
        }
      }
      .last()
  }
}
