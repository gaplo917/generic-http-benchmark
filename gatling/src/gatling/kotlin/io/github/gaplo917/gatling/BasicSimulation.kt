package io.github.gaplo917.gatling

import io.gatling.javaapi.core.*
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*
import io.netty.handler.codec.http.HttpResponseStatus
import java.lang.IllegalArgumentException

class BasicSimulation : Simulation() {

    /**
     * port defined in the docker-compose.yaml
     */
    private val webFluxHost = "http://localhost:8080"
    private val mvcHost = "http://localhost:8081"
    private val ktorHost = "http://localhost:8082"
    private val nestJsHost = "http://localhost:8083"

    private val concurrency = 2_500

    private val httpProtocol = http
        .shareConnections()
        .connectionHeader("keep-alive")
        .disableCaching()
        .acceptEncodingHeader("gzip, deflate")
        .userAgentHeader("gatling")
        .maxConnectionsPerHost(1)

    private val ioDelayVariants = listOf<Int>(
        25,
        50,
        100,
        200
    )
    private val scenarios =
        createScenarios(
            ioDelayVariants = ioDelayVariants,
            approximateNumOfRequest = { ioDelay -> goodPerformance(ioDelay) },
            concurrency = concurrency,
            name = { ioDelay -> "${ioDelay * 2}ms-ktor-non-blocking" },
            endpoint = { ioDelay -> "$ktorHost/ktor-non-blocking/${ioDelay}" }
        ) +
                createScenarios(
                    ioDelayVariants = ioDelayVariants,
                    approximateNumOfRequest = { ioDelay -> goodPerformance(ioDelay) },
                    concurrency = concurrency,
                    name = { ioDelay -> "${ioDelay * 2}ms-nestjs-non-blocking" },
                    endpoint = { ioDelay -> "$nestJsHost/nestjs-non-blocking/${ioDelay}" }
                ) +
                createScenarios(
                    ioDelayVariants = ioDelayVariants,
                    approximateNumOfRequest = { ioDelay -> goodPerformance(ioDelay) },
                    concurrency = concurrency,
                    name = { ioDelay -> "${ioDelay * 2}ms-webflux-non-blocking-coroutine" },
                    endpoint = { ioDelay -> "$webFluxHost/webflux-non-blocking-coroutine/$ioDelay" }
                ) +
                createScenarios(
                    ioDelayVariants = ioDelayVariants,
                    approximateNumOfRequest = { ioDelay -> goodPerformance(ioDelay) },
                    concurrency = concurrency,
                    name = { ioDelay -> "${ioDelay * 2}ms-webflux-non-blocking-reactor" },
                    endpoint = { ioDelay -> "$webFluxHost/webflux-non-blocking-reactor/$ioDelay" }
                ) +
                createScenarios(
                    ioDelayVariants = ioDelayVariants,
                    approximateNumOfRequest = { ioDelay -> goodPerformance(ioDelay) },
                    concurrency = concurrency,
                    name = { ioDelay -> "${ioDelay * 2}ms-webflux-non-blocking-reactor-structured-concurrency" },
                    endpoint = { ioDelay -> "$webFluxHost/webflux-non-blocking-reactor-structured-concurrency/$ioDelay" }
                ) +
                createScenarios(
                    ioDelayVariants = ioDelayVariants,
                    approximateNumOfRequest = { ioDelay -> goodPerformance(ioDelay) },
                    concurrency = concurrency,
                    name = { ioDelay -> "${ioDelay * 2}ms-webflux-blocking-structured-concurrency" },
                    endpoint = { ioDelay -> "$webFluxHost/webflux-blocking-structured-concurrency/$ioDelay" }
                ) +
                createScenarios(
                    ioDelayVariants = ioDelayVariants,
                    approximateNumOfRequest = { ioDelay -> goodPerformance(ioDelay) },
                    concurrency = concurrency,
                    name = { ioDelay -> "${ioDelay * 2}ms-webflux-blocking-vt-reactor" },
                    endpoint = { ioDelay -> "$webFluxHost/webflux-blocking-vt-reactor/$ioDelay" }
                ) +
                createScenarios(
                    ioDelayVariants = ioDelayVariants,
                    approximateNumOfRequest = { ioDelay -> goodPerformance(ioDelay) },
                    concurrency = concurrency,
                    name = { ioDelay -> "${ioDelay * 2}ms-webflux-blocking-vt-coroutine" },
                    endpoint = { ioDelay -> "$webFluxHost/webflux-blocking-vt-coroutine/$ioDelay" }
                ) +
                createScenarios(
                    ioDelayVariants = ioDelayVariants,
                    approximateNumOfRequest = { ioDelay -> poorPerformance(ioDelay) },
                    concurrency = concurrency,
                    name = { ioDelay -> "${ioDelay * 2}ms-mvc-blocking" },
                    endpoint = { ioDelay -> "$mvcHost/mvc-blocking/$ioDelay" }
                ) +
                createScenarios(
                    ioDelayVariants = ioDelayVariants,
                    approximateNumOfRequest = { ioDelay -> goodPerformance(ioDelay) },
                    concurrency = concurrency,
                    name = { ioDelay -> "${ioDelay * 2}ms-mvc-blocking-vt-future" },
                    endpoint = { ioDelay -> "$mvcHost/mvc-blocking-vt-future/$ioDelay" }
                ) +
                createScenarios(
                    ioDelayVariants = ioDelayVariants,
                    approximateNumOfRequest = { ioDelay -> goodPerformance(ioDelay) },
                    concurrency = concurrency,
                    name = { ioDelay -> "${ioDelay * 2}ms-mvc-blocking-vt-coroutine" },
                    endpoint = { ioDelay -> "$mvcHost/mvc-blocking-vt-coroutine/$ioDelay" }
                ) +
                createScenarios(
                    ioDelayVariants = ioDelayVariants,
                    approximateNumOfRequest = { ioDelay -> goodPerformance(ioDelay) },
                    concurrency = concurrency,
                    name = { ioDelay -> "${ioDelay * 2}ms-mvc-non-blocking-future" },
                    endpoint = { ioDelay -> "$mvcHost/mvc-non-blocking-future/$ioDelay" }
                ) +
                createScenarios(
                    ioDelayVariants = ioDelayVariants,
                    approximateNumOfRequest = { ioDelay -> goodPerformance(ioDelay) },
                    concurrency = concurrency,
                    name = { ioDelay -> "${ioDelay * 2}ms-mvc-non-blocking-coroutine" },
                    endpoint = { ioDelay -> "$mvcHost/mvc-non-blocking-coroutine/$ioDelay" }
                )


    private fun goodPerformance(ioDelay: Int): Int {
        return when (ioDelay) {
            25 -> 100_000
            50 -> 100_000
            100 -> 100_000
            200 -> 100_000
            else -> throw IllegalArgumentException("not supported ioDelay number")
        }
    }

    /**
     * When the scenario QPS is very low, we don't want to test a large number of result
     */
    private fun poorPerformance(ioDelay: Int): Int {
        return when (ioDelay) {
            25 -> 100_000
            50 -> 100_000
            100 -> 50_000
            200 -> 25_000
            else -> throw IllegalArgumentException("not supported ioDelay number")
        }
    }

    private fun createScenarios(
        ioDelayVariants: List<Int>,
        approximateNumOfRequest: (Int) -> Int,
        concurrency: Int,
        name: (Int) -> String,
        endpoint: (Int) -> String
    ): List<PopulationBuilder> {
        return ioDelayVariants.map { ioDelay ->
            val repeat = (approximateNumOfRequest(ioDelay) / concurrency).coerceAtLeast(1)

            scenario(name(ioDelay))
                // requests for warming up
                .repeat(repeat)
                .on(
                    exec(
                        http(name(ioDelay)).get(endpoint(ioDelay))
                            .requestTimeout(30)
                            .check(status().`is`(200))
                            .silent()
                    )
                )
                // actual measure
                .repeat(repeat)
                .on(
                    exec(
                        http(name(ioDelay)).get(endpoint(ioDelay))
                            .requestTimeout(30)
                            .check(status().`is`(200))
                    )
                )
                .injectOpen(
                    nothingFor(10),
                    constantUsersPerSec(concurrency / 10.0).during(10)
                )
        }
    }

    init {
        setUp(
            scenarios.reduce { acc, populationBuilder ->
                acc.andThen(populationBuilder)
            }
        ).protocols(httpProtocol)
    }
}

