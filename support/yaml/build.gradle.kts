import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
}

tasks {
    withType<Jar> { enabled = true }
    withType<BootJar> { enabled = false }
}
