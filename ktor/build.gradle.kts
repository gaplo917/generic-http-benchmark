val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val prometeus_version: String by project

plugins {
    application
    kotlin("jvm")
    id("io.ktor.plugin") version "2.1.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.21"
    id("com.google.cloud.tools.jib")
}

group = "io.github.gaplo917"
version = "0.0.1"
application {
    mainClass.set("io.github.gaplo917.ktor.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm:$ktor_version")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometeus_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

jib {
    from {
        image = "amazoncorretto:19.0.1"
        platforms {
            platform {
                architecture = System.getenv("PLATFORM") ?: "arm64" // switch to "amd64" if Intel/AMD CPU
                os = "linux"
            }
        }
    }
    to {
        image = "ktor-benchmark"
        tags = setOf("latest")
    }
    container {
        environment = mapOf(
            "JAVA_TOOL_OPTIONS" to "-XX:+UseZGC -Xmx3G --enable-preview --add-modules=jdk.incubator.concurrent",
        )
    }
}
