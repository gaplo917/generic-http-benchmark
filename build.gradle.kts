import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm") version "1.7.20"
}

allprojects {
    group = "io.github.gaplo917"

    repositories { mavenCentral() }

    apply {
        plugin("kotlin")
    }

    repositories { mavenCentral() }

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
}
