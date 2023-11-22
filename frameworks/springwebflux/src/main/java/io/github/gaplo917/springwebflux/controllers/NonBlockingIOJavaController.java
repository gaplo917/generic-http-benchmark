package io.github.gaplo917.springwebflux.controllers;

import io.github.gaplo917.springwebflux.data.DummyResponse;
import io.github.gaplo917.springwebflux.services.IOService;
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
import java.util.concurrent.StructuredTaskScope;

@Controller
public class NonBlockingIOJavaController {
  private final ExecutorService vtExecutor = Executors.newVirtualThreadPerTaskExecutor();

  private final IOService ioService;

  NonBlockingIOJavaController(IOService ioService) {
    this.ioService = ioService;
  }

  @RequestMapping("/webflux-nio-reactor/{ioDelay}")
  public Mono<ResponseEntity<List<DummyResponse>>> nonBlockingReactor(
      @PathVariable Long ioDelay
  ) {
    return ioService.nonBlockingIO(ioDelay)
        .flatMap(resp1 -> ioService.dependentNonBlockingIO(ioDelay, resp1))
        .map(ResponseEntity::ok);
  }

  @RequestMapping("/webflux-nio-reactor-parallel/{ioDelay}")
  public Mono<ResponseEntity<List<DummyResponse>>> nonBlockingReactorParallelIOApi(
      @PathVariable Long ioDelay
  ) {
    return Mono.zip(
        ioService.nonBlockingIO(ioDelay),
        ioService.nonBlockingIO(ioDelay),
        List::of
    ).map(ResponseEntity::ok);
  }

  @RequestMapping("/webflux-nio-reactor-structured-concurrency/{ioDelay}")
  public Mono<ResponseEntity<List<DummyResponse>>> nonBlockingStructuredTaskScopeApi(
      @PathVariable Long ioDelay
  ) {
    return Mono.<List<DummyResponse>>create(observer -> {
          try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            final var future1 = scope.fork(() -> {
              final var resp = ioService.nonBlockingIO(ioDelay)
                  .subscribeOn(Schedulers.fromExecutor(vtExecutor))
                  .block();

              return ioService.dependentNonBlockingIO(ioDelay, resp)
                  .subscribeOn(Schedulers.fromExecutor(vtExecutor))
                  .block();
            });
            // Wait for all threads to finish or the task scope to shut down.
            // This method waits until all threads started in the task scope finish execution
            scope.join();
            scope.throwIfFailed();
            observer.success(future1.get());
          } catch (ExecutionException | InterruptedException e) {
            observer.error(e);
          }
        }).subscribeOn(Schedulers.fromExecutor(vtExecutor))
        .map(ResponseEntity::ok);
  }
}
