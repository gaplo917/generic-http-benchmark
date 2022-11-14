## Deep Dive the Real-World Performance of Kotlin Coroutine (V.S. Reactor, Java Virtual Threads)

As all we know, Kotlin Coroutine has provided us a good way to structure our asynchronous codes 
in a readable sequence and it is also theoretically more CPU-friendly that reduces unnecessary context 
switching to run faster and allow the CPU to work more. However, theory is just the texts on paper, 
developers need real world scenarios and performance metrics to make better decisions.

I believe the following questions popped up in my mind will also in yours:

- How fast is Kotlin Coroutine compared to other popular asynchronous solutions?

- What are the performance differences if I misused Kotlin coroutine?

- What are the performance differences when the implementation runs heavy-IO and serialization operations?

- What are the performance differences when I switched to Kotlin Coroutine in Spring Boot?

- Hardware is cheap and eventually will be cheaper in the future, why should I consider Kotlin Coroutine?

This is a technical talk to deep dive the performance differences on Kotlin Coroutine, 
Java virtual threads, and Reactor backed by a list of JMH benchmarks on the JVM to answer those questions.


## Objective
This benchmark is to show the performance of using java virtual threads, 
kotlin coroutine, and reactor on practical scenarios.

Variables:

n - number of operations

threads - numbers of threads available in the thread pool size

opsMode - `noOps` | `Ops`, `noOps` is the baseline when there are no IO task and no serialization task while `ops` is the
real-world non-blocking IO operations and serialization operations.

Expected questions answered by the benchmark:

- What is the performance of running `n` **synchronous codes without any computational operations**
in synchronous function, suspend function with different coroutine contexts, and Reactor?

- What is the performance of running `n` **computational operations** 
in synchronous function, suspend function with different coroutine contexts, and Reactor?

- What is the performance of running `n` parallel **1-4ms IO operations 
followed by a JSON serialization and Map-merging operations** in suspend function, and Reactor
with different `threads`?

- What is the performance difference between `coroutineScope`, `withContext(Dispatchers.XXX)`, and `GlobalScope.launch`
  to wrap non-blocking IO call?

- WIP: What is the performance difference of wrapping blocking IO in java virtual threads (JDK 19) 
in Kotlin coroutine and Reactor versus using non-blocking IO directly? 
e.g. Wrapping `Thread.sleep(..)` into virtual threads to simulate JDBC IO V.S. `delay(..)` to simulate R2DBC IO

- WIP: What is the performance of java virtual threads compared to Kotlin coroutine and Reactor.

- WIP: What is the performance of context switching and scheduling in Kotlin Coroutine, Reactor,
  and java virtual threads?

## Getting Started (JMH)

```bash

# Run JMH benchmark
./gradlew jmh:mainBenchmark

```

## Getting Started (end-to-end Spring)

```bash
# Build Spring MVC Docker image
./gradlew springmvc:jibDockerBuild

# Build Spring WebFlux Docker Image
./gradlew springwebflux:jibDockerBuild

# Start the containers
docker compose up


```

## Benchmark Environment
WIP

## Benchmark Result
WIP

## Conclusion
WIP


