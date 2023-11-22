package io.github.gaplo917.springmvc.controllers;

import io.github.gaplo917.springmvc.services.IOService;
import io.github.gaplo917.springmvc.data.DummyResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.StructuredTaskScope;

@Controller
public class NonBlockingIOJavaController {
  private final IOService ioService;

  NonBlockingIOJavaController(IOService ioService) {
    this.ioService = ioService;
  }

  @RequestMapping("/mvc-nio-future/{ioDelay}")
  public Future<ResponseEntity<List<DummyResponse>>> nonBlockingFutureApi(@PathVariable Long ioDelay) {
    return ioService.nonBlockingIO(ioDelay)
        .thenCompose(resp -> ioService.dependentNonBlockingIO(ioDelay, resp))
        .thenApply(ResponseEntity::ok);
  }

  // require Spring MVC enabled virtual threads
  @RequestMapping("/mvc-nio-future-structured-concurrency-parallel/{ioDelay}")
  ResponseEntity<List<DummyResponse>> nonBlockingFutureStructuredConcurrencyParallelIOApi(
      @PathVariable Long ioDelay
  ) throws InterruptedException, ExecutionException {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      final var future1 = scope.fork(() -> ioService.nonBlockingIO(ioDelay).get());
      final var future2 = scope.fork(() -> ioService.nonBlockingIO(ioDelay).get());
      // Wait for all threads to finish or the task scope to shut down.
      // This method waits until all threads started in the task scope finish execution
      scope.join();
      scope.throwIfFailed();
      return ResponseEntity.ok(List.of(future1.get(), future2.get()));
    }
  }

}
