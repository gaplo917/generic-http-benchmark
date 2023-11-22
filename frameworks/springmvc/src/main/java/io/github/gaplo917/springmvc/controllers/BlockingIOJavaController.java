package io.github.gaplo917.springmvc.controllers;

import io.github.gaplo917.springmvc.services.IOService;
import io.github.gaplo917.springmvc.data.DummyResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.concurrent.*;

@Controller
public class BlockingIOJavaController {
  private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
  private final IOService ioService;

  BlockingIOJavaController(IOService ioService) {
    this.ioService = ioService;
  }

  @RequestMapping("/mvc-bio/{ioDelay}")
  public ResponseEntity<List<DummyResponse>> blockingApi(
      @PathVariable Long ioDelay
  ) {
    final var resp1 = ioService.blockingIO(ioDelay);
    final var result = ioService.dependentBlockingIO(ioDelay, resp1);
    return ResponseEntity.ok(result);
  }

  @RequestMapping("/mvc-bio-future-in-vt/{ioDelay}")
  public Future<ResponseEntity<List<DummyResponse>>> blockingFutureInVTApi(
      @PathVariable Long ioDelay
  ) {
    return CompletableFuture.supplyAsync(() -> {
          final var resp1 = ioService.blockingIO(ioDelay);
          return ioService.dependentBlockingIO(ioDelay, resp1);
        },
        virtualThreadExecutor
    ).thenApplyAsync(ResponseEntity::ok);
  }

  // required Spring MVC to work in virtual threads pool
  @RequestMapping("/mvc-bio-structured-concurrency-parallel/{ioDelay}")
  public ResponseEntity<List<DummyResponse>> blockingNativeJavaStructuredConcurrencyApi(
      @PathVariable Long ioDelay
  ) throws InterruptedException, ExecutionException {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      final var future1 = scope.fork(() -> ioService.blockingIO(ioDelay));
      final var future2 = scope.fork(() -> ioService.blockingIO(ioDelay));
      // Wait for all threads to finish or the task scope to shut down.
      // This method waits until all threads started in the task scope finish execution
      scope.join();
      scope.throwIfFailed();
      return ResponseEntity.ok(List.of(future1.get(), future2.get()));
    }
  }
}
