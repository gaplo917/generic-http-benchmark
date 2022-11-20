package io.github.gaplo917.springwebflux.controllers;

import io.github.gaplo917.springwebflux.data.DummyResponse;
import io.github.gaplo917.springwebflux.services.IOService;
import jdk.incubator.concurrent.StructuredTaskScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
public class BlockingIOJavaController {
  private ExecutorService vtExecutor = Executors.newVirtualThreadPerTaskExecutor();

  private IOService ioService;

  BlockingIOJavaController(IOService ioService) {
    this.ioService = ioService;
  }


  @RequestMapping("/webflux-bio/{ioDelay}")
  public ResponseEntity<List<DummyResponse>> blockingApi(@PathVariable Long ioDelay) {
    final var resp1 = ioService.blockingIO(ioDelay);
    final var result = ioService.dependentBlockingIO(ioDelay, resp1);
    return ResponseEntity.ok(result);
  }

  @RequestMapping("/webflux-bio-reactor-in-vt/{ioDelay}")
  public Mono<ResponseEntity<List<DummyResponse>>> blockingVirtualThreadReactorApi(
      @PathVariable Long ioDelay
  ) {
    return Mono.<List<DummyResponse>>create(observer -> {
          final var resp1 = ioService.blockingIO(ioDelay);
          final var result = ioService.dependentBlockingIO(ioDelay, resp1);
          observer.success(result);
        }).subscribeOn(Schedulers.fromExecutor(vtExecutor))
        .map(result -> ResponseEntity.ok(result));
  }


  @RequestMapping("/webflux-bio-structured-concurrency-parallel/{ioDelay}")
  public Mono<ResponseEntity<List<DummyResponse>>> blockingNativeVirtualThreadParallelIOApi(
      @PathVariable Long ioDelay
  ) {
    return Mono.<List<DummyResponse>>create(observer -> {
          try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            final var future1 = scope.fork(() -> ioService.blockingIO(ioDelay));
            final var future2 = scope.fork(() -> ioService.blockingIO(ioDelay));
            // Wait for all threads to finish or the task scope to shut down.
            // This method waits until all threads started in the task scope finish execution
            scope.join();
            scope.throwIfFailed();
            observer.success(List.of(future1.resultNow(), future2.resultNow()));
          } catch (ExecutionException e) {
            observer.error(e);
          } catch (InterruptedException e) {
            observer.error(e);
          }
        }).subscribeOn(Schedulers.fromExecutor(vtExecutor))
        .map(result -> ResponseEntity.ok(result));
  }
}
