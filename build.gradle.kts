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

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "19" }

tasks.withType<JavaExec> {
  environment("JAVA_TOOL_OPTIONS", "--enable-preview --add-modules=jdk.incubator.concurrent")
}

application { mainClass.set("MainKt") }

sourceSets {
  create("benchmarks") { java.srcDir("src/benchmarks/kotlin") }
}

kotlin {
  jvmToolchain { languageVersion.set(JavaLanguageVersion.of(19)) }

  sourceSets {
    getByName("benchmarks") {
      dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.4")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")

        // netty eventloop
        implementation("io.netty:netty-all:4.1.84.Final")
      }
    }
  }
}

benchmark {
  configurations {
    getByName("main") {
//      // cleanest compute ops benchmark
//      include("SequentialChainComputeOps")
//      include("CoroutineDispatcher")
//      include("CoroutineWrapComputeOps")
//      include("ReactorScheduler")
//      include("ReactorWrapComputeOps")
//
//      // cleanest non-blocking callback wrapper benchmark
      include("CoroutineWrapNonBlockingIO")
//      include("ReactorWrapNonBlockingIO")
//
//      // cleanest blocking wrapper benchmark
//      include("CoroutineWrapBlockingIOWithDispatcher")
//      include("CoroutineWrapBlockingIOWithJava19VirtualThread")
//      include("CoroutineWrapBlockingIOWithLargeThreadPool")
//      include("Java19VirtualThreadWrapBlockingIO")

      // spring boot
//      include("SpringBootMVCBenchmark")

      warmups = 2 // number of warmup iterations
      iterations = 2 // number of iterations
      iterationTime = 1000 // time in seconds per iteration
      iterationTimeUnit = "ms"
    }
  }
  targets { register("benchmarks") }
}
