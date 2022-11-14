
plugins {
  id("org.springframework.boot")
  id("io.spring.dependency-management")
  kotlin("jvm")
  kotlin("plugin.spring")
  id ("com.google.cloud.tools.jib")
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

  testImplementation("org.springframework.boot:spring-boot-starter-test")
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
    image = "spring-mvc-benchmark"
    tags = setOf("latest")
  }
  container {
    environment = mapOf("JAVA_TOOL_OPTIONS" to "--enable-preview --add-modules=jdk.incubator.concurrent")
  }
}
