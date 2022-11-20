import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm") version "1.7.20"
}

allprojects {
    group = "io.github.gaplo917"

    repositories {
        maven { url = uri("https://repo.spring.io/milestone") }
        mavenCentral()
    }

    apply {
        plugin("kotlin")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    kotlin { jvmToolchain { languageVersion.set(JavaLanguageVersion.of(19)) } }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("--enable-preview")
        options.compilerArgs.add("--add-modules=jdk.incubator.concurrent")
        options.release.set(19)
    }

    tasks.withType<JavaExec> {
        environment("JAVA_TOOL_OPTIONS", "--enable-preview --add-modules=jdk.incubator.concurrent")
    }
}
