plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
    id("com.google.cloud.tools.jib")
}

version = "0.0.1-SNAPSHOT"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.kotlinx.coroutines.jdk8)

    // integration with grafana
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs = listOf("--enable-preview", "--add-modules=jdk.incubator.concurrent")
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
        image = "spring-mvc-benchmark"
        tags = setOf("latest")
    }
    container {
        environment = mapOf(
            "JAVA_TOOL_OPTIONS" to "-XX:+UseZGC -Xmx4G --enable-preview --add-modules=jdk.incubator.concurrent",
        )
    }
}
