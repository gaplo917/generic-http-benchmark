package io.github.gaplo917.springwebflux.services;

import io.github.gaplo917.springwebflux.data.DummyResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class IOService {
  private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

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

  public Mono<DummyResponse> nonBlockingIO(Long ioDelay) {
    return Mono.create(observer ->
        scheduledExecutorService.schedule(
            () -> observer.success(DummyResponse.dummy(null)),
            ioDelay,
            TimeUnit.MILLISECONDS
        )
    );
  }

  public Mono<List<DummyResponse>> dependentNonBlockingIO(Long ioDelay, DummyResponse resp) {
    return Mono.create(observer ->
        scheduledExecutorService.schedule(
            () -> observer.success(List.of(resp, DummyResponse.dummy(null))),
            ioDelay,
            TimeUnit.MILLISECONDS
        )
    );
  }

}
