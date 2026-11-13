import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    java
    alias(libs.plugins.spring.boot)
}

group = "dev.cbuelvasc"
version = "0.1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // BOMs as Gradle platforms (no dependency-management plugin needed)
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    implementation(platform(libs.spring.ai.bom))

    // Driving adapters
    implementation(libs.spring.ai.mcp.server.webmvc)   // MCP (AI agents)
    implementation(libs.spring.boot.starter.webmvc)    // REST (humans)

    // Observability
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.micrometer.prometheus)

    testImplementation(platform(SpringBootPlugin.BOM_COORDINATES))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.archunit.junit5)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}

tasks.test {
    useJUnitPlatform()
}
