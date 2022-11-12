import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.springframework.boot") version "2.7.5"
  id("io.spring.dependency-management") version "1.1.0"
  kotlin("jvm") version "1.7.20"
  kotlin("plugin.spring") version "1.7.20"
  id ("com.google.cloud.tools.jib") version "3.2.0"
}

group = "io.github.gaplo917"

version = "0.0.1-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "17"
  }
}

tasks.withType<Test> { useJUnitPlatform() }

kotlin { jvmToolchain { languageVersion.set(JavaLanguageVersion.of(19)) } }

tasks.withType<JavaExec> {
  environment("JAVA_TOOL_OPTIONS", "--enable-preview --add-modules=jdk.incubator.concurrent")
}

jib {
  from {
    image = "amazoncorretto:19"
    platforms {
      platform {
        architecture = "arm64" // switch to "amd64" if Intel/AMD CPU
        os = "linux"
      }
    }
  }
  to {
    image = "spring-blocking"
    tags = setOf("latest")
  }
  container {
    creationTime = "USE_CURRENT_TIMESTAMP"
    environment = mapOf("JAVA_TOOL_OPTIONS" to "--enable-preview --add-modules=jdk.incubator.concurrent")
  }
}
