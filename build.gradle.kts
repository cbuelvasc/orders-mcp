import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    java
    alias(libs.plugins.spring.boot)
}

group = "dev.cbuelvasc"
version = "0.4.0"

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
    developmentOnly(platform(SpringBootPlugin.BOM_COORDINATES))
    testImplementation(platform(SpringBootPlugin.BOM_COORDINATES))

    // Driving adapters
    implementation(libs.spring.ai.mcp.server.webmvc)        // MCP (AI agents)
    implementation(libs.spring.boot.starter.webmvc)         // REST (humans)

    // Security: OAuth 2.1 resource server (part 2)
    implementation(libs.spring.boot.starter.oauth2.resource.server)

    // Driven adapter: PostgreSQL (part 3)
    implementation(libs.spring.boot.starter.jdbc)
    implementation(libs.spring.boot.starter.flyway)
    implementation(libs.flyway.postgresql)
    runtimeOnly(libs.postgresql)
    developmentOnly(libs.spring.boot.docker.compose)        // bootRun starts compose.yaml

    // Observability (part 4)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.micrometer.prometheus)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.archunit.junit5)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}

tasks.test {
    useJUnitPlatform()
}
