## Deep Dive the Real-World Performance of Kotlin Coroutine (V.S. Reactor, Java Virtual Threads)

As all we know, Kotlin Coroutine has provided us a good way to structure our asynchronous codes
in a readable sequence. It is also theoretically more CPU-friendly that reduces unnecessary context
switching to run faster and allow the CPU to work more. However, theory is just the texts on paper,
developers need real world scenarios and performance metrics to make better decisions.

> When we talk about "Real World Performance", we should measure both **machines** and **developers**.

I believe the following questions popped up in my mind will also in yours:

In machine world,

- How fast is Kotlin Coroutine compared to other popular asynchronous solutions in machine?

- What are the performance differences if I misused Kotlin coroutine?

- What are the performance differences when the application runs heavy-blocking-IO or
  heavy-non-blocking-IO operations?

- What are the performance differences when I switched to Kotlin Coroutine in Spring Boot MVC?

- Hardware is cheap and eventually will be cheaper in the future, why should I consider Kotlin Coroutine?

In developer world,

- What are the learning curve of writing performant codes in Kotlin Coroutine compared to Java Reactor?

- What changes required to apply Kotlin Coroutine into existing Spring Boot project?

- Java virtual threads and Structured Concurrency feature are coming to future Java, why should I consider Kotlin Coroutine?

This is a technical talk to deep dive the performance differences on Kotlin Coroutine,
Java virtual threads, and Reactor backed by a list of JMH benchmarks on the JVM to answer those questions.

## Objective

This benchmark is to show the performance of using Kotlin Coroutine, Reactor, and
Java virtual threads on both ideal(JMH) and real world(end-to-end) scenarios.

1. Find out the performance differences between the Reactor and Kotlin Coroutine.
2. Find out the performance differences when handling blocking IO and non-blocking IO.
3. Find out the performance differences in the future state of JVM (combined Kotlin Coroutine and Reactor with JDK 19 virtual threads)
4. Find out the cost of context switching and scheduling in Kotlin Coroutine, Reactor, and java virtual threads.
5. Compare the JVM-based(ktor, spring boot, vert.x) Http server performances in heavy non-blocking IO
6. Compare with nodejs and Go Http server performances in heavy non-blocking IO

## Getting Started (JMH)

Use JMH to run micro-benchmarks on Kotlin Coroutine, Virtual Threads, and Reactor.

```bash

# Run JMH benchmark
cd jmh;
./gradlew mainBenchmark;

```

## Getting Started (end-to-end HTTP benchmark)

All benchmark targets are designed to run in Docker container and use [Gatling](https://gatling.io/)
to run the load test and get the benchmark of end-to-end result.

![cover](cover.png)

### 1. Define benchmark scenarios in `./config/*.env`

In `./config/*.env`, there are key configurations to control gatling's concurrency.
![duration](duration-explained.png)

### 2. Start up the benchmark

First, you need to build the docker image for each application you want to test.

You might need at least 7 vCPU and 16GB Memory resources for the whole docker engine.

Run a single benchmark with the follow commands, it will build automatically if there is no available images.

```bash
# In case you need to change architecture to build cross platform
DOCKER_DEFAULT_PLATFORM=linux/arm64/v8 # Mac M1/M2 CPU
DOCKER_DEFAULT_PLATFORM=linux/amd64 # Intel / AMD CPU

# Run single benchmark (i.e. ktor)
ENV_FILE=./config/spring-mvc-16k.env
docker compose --env-file $ENV_FILE build && \
docker compose --env-file $ENV_FILE up -d benchmark-target && \
docker compose --env-file $ENV_FILE up gatling-runner && \
docker compose --env-file $ENV_FILE down
```

OR run all benchmarks through the scripts

```bash
# Run all benchmarks, config available in `./config/`
sh all-gatling-benchmarks.sh
```

it will output all the `gatling-runner` log to `./logs/`

### 4. Study the Gatling reports

Use the `./logs/ to trace the benchmark report in `./reports/`. Hope you would have a
great understanding on your application!

### (Optional) Grafana Dashboard to view application metrics

1. Un-comment the `prometheus` and `grafana` services in `docker-compose.yaml`
2. Make sure the application support prometheus, and add job and endpoints in `prometheus.yml`

```bash
ENV_FILE=./config/ktor.env
docker compose --env-file $ENV_FILE up -d benchmark-target prometheus grafana &&
```

Go to http://localhost:3000 to configure the grafana dashboard.

## Benchmark Environment

(Coming Soon)

## Benchmark Result

(Coming Soon)

## Conclusion

(Coming Soon)

## Troubleshooting

WIP

## Develop Gatling Kotlin Project

After making Gatling code changes, make sure you run to build the docker image.

```bash
docker compose --env-file $ENV_FILE build gatling-runner
```

## Contribution

1. Add new web framework `XXX` implementation that is equivalent to others languages.
2. Add gatling benchmark configuration in `./config/xxx.env`

## Troubleshooting

```bash
# Remove orphans containers
docker compose --env-file ./config/ktor.env down --remove-orphans
```
