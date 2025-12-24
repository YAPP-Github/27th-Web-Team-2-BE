import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":support:logging"))
    implementation(project(":support:yaml"))

    implementation("org.springframework.boot:spring-boot-starter-web")
}

tasks {
    withType<Jar> { enabled = true }
    withType<BootJar> { enabled = false }
}
