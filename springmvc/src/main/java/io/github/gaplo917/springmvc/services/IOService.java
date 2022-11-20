package io.github.gaplo917.springmvc.services;

import io.github.gaplo917.springmvc.data.DummyResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

@Service
public class IOService {
  private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

  public DummyResponse blockingIO(Long ioDelay) {
    try {
      Thread.sleep(ioDelay);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return DummyResponse.dummy(null);
  }

  public List<DummyResponse> dependentBlockingIO(Long ioDelay, DummyResponse resp) {
    try {
      Thread.sleep(ioDelay);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return List.of(resp, DummyResponse.dummy(null));
  }

  public CompletableFuture<DummyResponse> nonBlockingIO(Long ioDelay) {
    return CompletableFuture.supplyAsync(
        () -> DummyResponse.dummy(null),
        CompletableFuture.delayedExecutor(ioDelay, TimeUnit.MILLISECONDS, scheduledExecutorService)
    );
  }

  public CompletableFuture<List<DummyResponse>> dependentNonBlockingIO(Long ioDelay, DummyResponse resp) {
    return CompletableFuture.supplyAsync(
        () -> List.of(resp, DummyResponse.dummy(null)),
        CompletableFuture.delayedExecutor(ioDelay, TimeUnit.MILLISECONDS, scheduledExecutorService)
    );
  }

}
