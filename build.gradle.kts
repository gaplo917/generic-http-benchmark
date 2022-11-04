import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.7.10"
  id("org.jetbrains.kotlinx.benchmark") version "0.4.4"
  id("org.jetbrains.kotlin.plugin.allopen") version "1.7.10"
  id("org.jetbrains.kotlin.plugin.serialization") version "1.7.10"
  application
}

allOpen { annotation("org.openjdk.jmh.annotations.State") }

group = "io.github.gaplo917"

version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies { testImplementation(kotlin("test")) }

tasks.test { useJUnitPlatform() }

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }

application { mainClass.set("MainKt") }

sourceSets { create("benchmarks") { java.srcDir("src/benchmarks/kotlin") } }

kotlin {
  sourceSets {
    getByName("benchmarks") {
      dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.4")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
      }
    }
  }
}

benchmark {
  configurations {
    getByName("main") {
      // cleanest no ops benchmark
      include("CoroutineDispatcher")
      include("CoroutineSuspendWrap")
      include("ReactorScheduler")
      include("ReactorWrapper")

      // cleanest no ops non-blocking callback wrapper benchmark
      include("CoroutineSuspendNonBlockingWrapper")
      include("ReactorNonBlockingWrapper")

      // TODO: cleanest no ops blocking wrapper benchmark

      // real world case
      include("OneByOneOps")
      include("ParallelCoroutineOps")
      include("ParallelReactorOps")

      warmups = 2 // number of warmup iterations
      iterations = 2 // number of iterations
      iterationTime = 500 // time in seconds per iteration
      iterationTimeUnit = "ms"
    }
  }
  targets { register("benchmarks") }
}
