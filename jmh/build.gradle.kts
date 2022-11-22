plugins {
    kotlin("jvm") version "1.7.20"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.4"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.7.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.20"
    application
}

allOpen { annotation("org.openjdk.jmh.annotations.State") }

version = "1.0-SNAPSHOT"

repositories {
    maven { url = uri("https://repo.spring.io/milestone") }
    mavenCentral()
}

dependencies { testImplementation(kotlin("test")) }

application { mainClass.set("MainKt") }

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")

    // netty eventloop
    implementation("io.netty:netty-all:4.1.84.Final")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
    options.compilerArgs.add("--add-modules=jdk.incubator.concurrent")
    options.release.set(19)
}

tasks.withType<JavaExec> {
    environment("JAVA_TOOL_OPTIONS", "--enable-preview --add-modules=jdk.incubator.concurrent")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs = listOf("--enable-preview", "--add-modules=jdk.incubator.concurrent")
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
