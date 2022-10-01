import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.4"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.7.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.10"
    application
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

group = "io.github.gaplo917"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}

sourceSets {
    create("benchmarks") {
        java.srcDir("src/benchmarks/kotlin")
    }
}

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
    targets {
        register("benchmarks")
    }
}
