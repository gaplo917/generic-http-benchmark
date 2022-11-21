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

```bash

# Run JMH benchmark
./gradlew jmh:mainBenchmark

```

## Getting Started (end-to-end Gatling)

### 1. Docker build

`amd64` CPU Architecture (e.g. Intel / AMD CPU)

```bash
# Build Spring MVC Docker image
PLATFORM=amd64 ./gradlew springmvc:jibDockerBuild

# Build Spring WebFlux Docker image
PLATFORM=amd64 ./gradlew springwebflux:jibDockerBuild

# Build Ktor Docker image
PLATFORM=amd64 ./gradlew ktor:jibDockerBuild

# Build nestjs
DOCKER_DEFAULT_PLATFORM=linux/arm64/v8 docker compose --env-file ./config/nestjs.env build

# Build actixweb(Rust)
DOCKER_DEFAULT_PLATFORM=linux/arm64/v8 docker compose --env-file ./config/actixweb.env build

# Build warp(Rust)
DOCKER_DEFAULT_PLATFORM=linux/arm64/v8 docker compose --env-file ./config/warp.env build

```

OR `arm64` CPU (e.g. M1 Mac)

```bash
# Build Spring MVC Docker image
PLATFORM=arm64 ./gradlew springmvc:jibDockerBuild

# Build Spring WebFlux Docker image
PLATFORM=arm64 ./gradlew springwebflux:jibDockerBuild

# Build Ktor Docker image
PLATFORM=arm64 ./gradlew ktor:jibDockerBuild

# Build nestjs(Nodejs)
DOCKER_DEFAULT_PLATFORM=linux/arm64/v8 docker compose --env-file ./config/nestjs.env build

# Build actixweb(Rust)
DOCKER_DEFAULT_PLATFORM=linux/arm64/v8 docker compose --env-file ./config/actixweb.env build

# Build warp(Rust)
DOCKER_DEFAULT_PLATFORM=linux/arm64/v8 docker compose --env-file ./config/warp.env build

```

### 2. Docker compose run

You might need at least 5 CPU and 12GB RAM for the whole docker engine.

```bash
# Run single benchmark (i.e. ktor)
ENV_FILE=./config/ktor.env
docker compose --env-file $ENV_FILE up -d benchmark-target && \
docker compose --env-file $ENV_FILE build gatling-runner && \
docker compose --env-file $ENV_FILE up gatling-runner && \
docker compose --env-file $ENV_FILE down
```

### 3. (Optional) Grafana Dashboard to view application metrics

1. Unhide the `prometheus` and `grafana` services in `docker-compose.yaml`
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
