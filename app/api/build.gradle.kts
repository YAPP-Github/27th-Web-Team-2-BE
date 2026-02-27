import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":support:logging"))
    implementation(project(":support:yaml"))
    implementation(project(":core"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

    implementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:2.4.0"))
    implementation("io.awspring.cloud:spring-cloud-starter-aws-parameter-store-config")

    implementation("io.sentry:sentry-spring-boot-starter-jakarta:8.28.0")
}

tasks {
    withType<Jar> { enabled = false }
    withType<BootJar> { enabled = true }
}
