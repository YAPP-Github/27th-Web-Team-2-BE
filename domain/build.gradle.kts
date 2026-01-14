import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
}

tasks {
    withType<Jar> { enabled = true }
    withType<BootJar> { enabled = false }
}
