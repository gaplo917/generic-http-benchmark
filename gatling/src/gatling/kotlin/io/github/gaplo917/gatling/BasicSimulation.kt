package io.github.gaplo917.gatling

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.PopulationBuilder
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

class BasicSimulation : Simulation() {

    data class Benchmark(
        val name: String,
        val endpoint: String,
        val peakConcurrencyList: List<Int>
    )

    private fun env(key: String): String? = System.getenv(key)?.also {
        println("Picked up $key=$it")
    }

    private val timeout by lazy {
        env("BENCHMARK_REQUEST_TIMEOUT")?.toInt() ?: 30
    }

    private val warmUpDuration by lazy {
        env("BENCHMARK_WARM_UP_DURATION")?.toLong() ?: 10L
    }

    private val rampUpDuration by lazy {
        env("BENCHMARK_RAMP_UP_DURATION")?.toLong() ?: 10L
    }

    private val sustainPeakDuration by lazy {
        env("BENCHMARK_SUSTAIN_PEAK_DURATION")?.toLong() ?: 10L
    }

    // example: 100000|50ms-mvc-blocking|http://spring-mvc-benchmark:8080/mvc-blocking/25
    private val scenariosInput by lazy {
        Array(10000) { index ->
            env("BENCHMARK_SCENARIOS_${index}")
        }
            .mapNotNull { it }
            .map { input ->
                val arr = input.split("|").map { it.trim() }
                Benchmark(
                    name = arr[1],
                    endpoint = arr[2],
                    peakConcurrencyList = arr[0].split(",").map { it.toInt() }
                )
            }
    }

    private val httpProtocol = http
        .shareConnections()
        .connectionHeader("keep-alive")
        .disableCaching()
        .acceptEncodingHeader("gzip, deflate")
        .userAgentHeader("gatling")
        .maxConnectionsPerHost(1)

    private val scenarios = scenariosInput.map { input ->
        createScenarios(
            peakConcurrencyList = input.peakConcurrencyList,
            name = input.name,
            endpoint = input.endpoint
        )
    }

    private fun createScenarios(
        peakConcurrencyList: List<Int>,
        name: String,
        endpoint: String,
    ): PopulationBuilder {

        val warmUp = scenario("warmup-$name")
            .during(5L)
            .on(
                exec(
                    http("warmup-$name").get(endpoint)
                        .requestTimeout(timeout)
                        .check(status().`is`(200))
                )
            )
            .injectClosed(
                rampConcurrentUsers(0).to(peakConcurrencyList.last()).during(warmUpDuration),
            )

        val actual = scenario(name)
            .during(5L)
            .on(
                exec(
                    http(name).get(endpoint)
                        .requestTimeout(timeout)
                        .check(status().`is`(200))
                )
            )
            .injectClosed(
                *peakConcurrencyList.flatMapIndexed { index, concurrency ->
                    val prevConcurrency = if (index == 0) 0 else peakConcurrencyList[index - 1]
                    listOf(
                        rampConcurrentUsers(prevConcurrency).to(concurrency).during(rampUpDuration),
                        constantConcurrentUsers(concurrency).during(sustainPeakDuration)
                    )
                }.toTypedArray()
            )

        return warmUp
            .andThen(actual)
    }

    init {
        setUp(scenarios.reduce { acc, e ->
            acc.andThen(e)
        }).protocols(httpProtocol)

    }
}

