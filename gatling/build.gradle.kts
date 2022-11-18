plugins {
    kotlin("jvm")
    id("io.gatling.gradle") version "3.8.4"
}

group = "io.github.gaplo917"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}


gatling {
    jvmArgs = listOf("-XX:+UseZGC", "-Xms512M", "-Xmx4G", "-XX:-MaxFDLimit")
}
