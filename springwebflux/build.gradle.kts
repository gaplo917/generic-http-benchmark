plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
    id("com.google.cloud.tools.jib")
}

group = "io.github.gaplo917"

version = "0.0.1-SNAPSHOT"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.kotlinx.coroutines.jdk8)

    // integration with grafana
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

jib {
    from {
        image = "amazoncorretto:19.0.1"
        platforms {
            platform {
                architecture = "arm64" // switch to "amd64" if Intel/AMD CPU
                os = "linux"
            }
        }
    }
    to {
        image = "spring-webflux-benchmark"
        tags = setOf("latest")
    }
    container {
        environment = mapOf(
            "JAVA_TOOL_OPTIONS" to "-XX:+UseZGC -Xmx4G --enable-preview --add-modules=jdk.incubator.concurrent",
        )
    }
}
