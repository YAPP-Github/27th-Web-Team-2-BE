import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"

    kotlin("jvm") version "1.9.24"
    kotlin("kapt") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
    kotlin("plugin.jpa") version "1.9.24"
    kotlin("plugin.allopen") version "1.9.24"
    kotlin("plugin.noarg") version "1.9.24"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
}

group = "com.nomoney"
version = "0.0.1-SNAPSHOT"

allprojects {
    tasks.withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
    all {
        exclude(group = "commons-logging", module = "commons-logging")
    }
}

repositories {
    mavenCentral()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks {
    withType<Jar> { enabled = true }
    withType<BootJar> { enabled = false }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-kapt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")

        testImplementation("io.kotest:kotest-runner-junit5:6.0.0.M1")
        testImplementation("io.kotest:kotest-assertions-core:6.0.0.M1")
        testImplementation("io.mockk:mockk:1.13.12")
    }

    // 제외할 서브 프로젝트의 이름 목록
    val excludedProjects = setOf(":lagacy")

    // 만약 현재 프로젝트가 제외할 목록에 포함되지 않았다면 공통 설정 적용
    if (project.path !in excludedProjects) {
        apply(plugin = "io.spring.dependency-management")

        tasks.test {
            useJUnitPlatform()
        }
    }
}
