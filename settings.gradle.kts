rootProject.name = "kotlin-coroutine-benchmark"

include("springmvc")
include("springwebflux")
include("jmh")
include("gatling")
include("ktor")

pluginManagement {
    plugins {
        id("kotlin") version "1.7.20"
        id("org.jetbrains.kotlinx.benchmark") version "0.4.4"
        id("org.jetbrains.kotlin.plugin.allopen") version "1.7.20"
        id("org.jetbrains.kotlin.plugin.serialization") version "1.7.20"

        id("org.springframework.boot") version "2.7.5"
        id("io.spring.dependency-management") version "1.1.0"
        kotlin("plugin.spring") version "1.7.20"
        id("com.google.cloud.tools.jib") version "3.3.1"
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("kotlinx-benchmark-runtime", "org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.4")
            library("kotlinx-coroutines-core", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            library("kotlinx-coroutines-jdk8", "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
            library("kotlinx-coroutines-reactor", "org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")
            library("kotlinx-serialization-json", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
        }
    }
}
