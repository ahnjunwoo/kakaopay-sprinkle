plugins {
    val kotlinVersion = "1.3.70"
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.springframework.boot") version "2.2.4.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"

    kotlin("plugin.allopen") version kotlinVersion
    kotlin("plugin.noarg") version kotlinVersion

    idea
}

group = "com.github.ssssssu12.kakaopay-sprinkle"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val springbootStarter = "org.springframework.boot:spring-boot-starter"
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("$springbootStarter-web")
    implementation("$springbootStarter-data-jpa")
    implementation("$springbootStarter-data-redis")
    implementation("org.springframework.integration:spring-integration-redis")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    // https://github.com/MicroUtils/kotlin-logging
    implementation("io.github.microutils:kotlin-logging:1.7.9")
    implementation("com.querydsl:querydsl-jpa:4.2.2")

    kapt("com.querydsl:querydsl-apt:4.2.2:jpa")
    runtimeOnly("mysql:mysql-connector-java")
    runtimeOnly("com.h2database:h2")

    testImplementation("$springbootStarter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    // https://github.com/nhaarman/mockito-kotlin
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testRuntimeOnly("com.h2database:h2")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

idea {
    module {
        val kaptMain = file("build/generated/source/kapt/main")
        sourceDirs.add(kaptMain)
        generatedSourceDirs.add(kaptMain)
    }
}

allOpen {
    annotation("javax.persistence.Entity")
}

noArg {
    annotation("javax.persistence.Entity")
}

tasks.test {
    useJUnitPlatform()
}