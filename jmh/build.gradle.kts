plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.benchmark")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("org.jetbrains.kotlin.plugin.serialization")
    application
}

allOpen { annotation("org.openjdk.jmh.annotations.State") }

version = "1.0-SNAPSHOT"

dependencies { testImplementation(kotlin("test")) }

application { mainClass.set("MainKt") }

dependencies {
    implementation(libs.kotlinx.benchmark.runtime)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.kotlinx.serialization.json)

    // netty eventloop
    implementation("io.netty:netty-all:4.1.84.Final")
}

benchmark {
    configurations {
        getByName("main") {
            // cleanest compute ops benchmark
            include("SequentialChainComputeOps")
            include("CoroutineDispatcher")
            include("CoroutineWrapComputeOps")
            include("ReactorScheduler")
            include("ReactorWrapComputeOps")

            // cleanest non-blocking callback wrapper benchmark
            include("CoroutineWrapNonBlockingIO")
            include("ReactorWrapNonBlockingIO")

            // cleanest blocking wrapper benchmark
            include("CoroutineWrapBlockingIOWithDispatcher")
            include("CoroutineWrapBlockingIOWithJava19VirtualThread")
            include("CoroutineWrapBlockingIOWithLargeThreadPool")
            include("Java19VirtualThreadWrapBlockingIO")

            warmups = 2 // number of warmup iterations
            iterations = 2 // number of iterations
            iterationTime = 1000 // time in seconds per iteration
            iterationTimeUnit = "ms"
        }
    }
    targets { register("main") }
}
