package io.github.gaplo917.idle

import io.github.gaplo917.helper.NoOps
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@State(Scope.Benchmark)
@Threads(1)
class ReactorWrapper : NoOps {

  @Benchmark
  fun _000_mono_just() {
    Mono.just(noOps()).block()
  }

  @Benchmark
  fun _001_mono_create() = Mono.create { it.success(noOps()) }.block()

  @Benchmark
  fun _002_mono_defer_and_create() = Mono.defer {
    Mono.create { it.success(noOps()) }
  }.block()

  @Benchmark
  fun _003_flux_create() = Flux.create {
    it.next(noOps())
    it.complete()
  }.blockLast()


}
