// import org.springframework.boot.gradle.tasks.bundling.BootJar
//
// plugins {
//    id("org.springframework.boot")
//    id("io.spring.dependency-management")
//    id("com.ewerk.gradle.plugins.querydsl") version "1.0.10"
//    kotlin("plugin.spring")
//    kotlin("plugin.jpa")
//    kotlin("kapt")
// }
//
// allOpen {
//    annotation("javax.persistence.Entity")
//    annotation("javax.persistence.MappedSuperclass")
//    annotation("javax.persistence.Embeddable")
// }
//
// noArg {
//    annotation("javax.persistence.Entity")
//    annotation("javax.persistence.MappedSuperclass")
//    annotation("javax.persistence.Embeddable")
// }
//
// dependencies {
//    implementation(project(":domain"))
//    implementation(project(":port"))
//    implementation(project(":support:yaml"))
//
//    api("org.springframework.boot:spring-boot-starter-data-jpa")
//    runtimeOnly("mysql:mysql-connector-java:8.0.33")
//    implementation("org.jetbrains.kotlin:kotlin-reflect")
//    implementation("org.flywaydb:flyway-core:10.14.0")
//    implementation("org.flywaydb:flyway-mysql:10.14.0")
//    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
//    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
// }
//
// tasks {
//    withType<Jar> { enabled = true }
//    withType<BootJar> { enabled = false }
// }
