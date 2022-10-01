# Kotlin Coroutine Benchmark 

## Objective
This benchmark is to show the performance of using java virtual threads, 
kotlin coroutine, and reactor on practical scenarios.

The benchmark result is able to give the answers of the following questions:

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


## Benchmark Environment
WIP

## Benchmark Result
WIP

## Conclusion
WIP


