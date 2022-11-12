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

repositories {
  mavenCentral()
  maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("io.projectreactor:reactor-test")
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
    image = "spring-non-blocking"
    tags = setOf("latest")
  }
  container {
    creationTime = "USE_CURRENT_TIMESTAMP"
    environment = mapOf("JAVA_TOOL_OPTIONS" to "--enable-preview --add-modules=jdk.incubator.concurrent")
  }
}
