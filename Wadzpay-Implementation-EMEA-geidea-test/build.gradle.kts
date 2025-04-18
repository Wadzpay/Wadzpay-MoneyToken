import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.4.32"
    kotlin("plugin.spring") version "1.4.32"
    kotlin("plugin.jpa") version "1.4.32"
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
    id("com.github.ben-manes.versions") version "0.36.0"
}

group = "com.vacuumlabs.wadzpay"
version = "0.0.4-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        name = "ktor-eap"
    }
    maven {
        url = uri("https://groovy.jfrog.io/artifactory/libs-release")
        name = "jfrog-groovy"
    }
}

val springdocVersion = "1.6.3"
val h2Version = "1.4.200"
val kotestVersion = "4.5.0"
val twilioVersion = "8.11.0"
val kotlinxCoroutinesVersion = "1.4.3"
val mockitoVersion = "3.9.0"
val mockkVersion = "1.10.6"
val springmockkVersion = "3.0.1"
val ktorVersion = "1.5.4"
val junitJupiterVersion = "5.4.2"
val testcontainersVersion = "1.15.3"
val flywayVersion = "7.11.0"
val openCsvVersion = "5.4"
val gsonVersion = "2.8.5"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework", "spring-aspects")
    implementation("org.springframework.data", "spring-data-redis")
    implementation("org.springframework.retry", "spring-retry")
    implementation("org.springframework.security", "spring-security-oauth2-resource-server")
    implementation("org.springframework.security", "spring-security-oauth2-jose")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springdoc", "springdoc-openapi-ui", springdocVersion)
    implementation("org.springdoc", "springdoc-openapi-kotlin", springdocVersion)
    implementation("com.h2database", "h2", h2Version)
    implementation("com.twilio.sdk", "twilio", twilioVersion)
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", kotlinxCoroutinesVersion)
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-reactor", kotlinxCoroutinesVersion)
    implementation("io.ktor", "ktor-client-core", ktorVersion)
    implementation("io.ktor", "ktor-client-cio", ktorVersion)
    implementation("io.ktor", "ktor-client-jackson", ktorVersion)
    implementation("org.flywaydb", "flyway-core", flywayVersion)
    implementation("io.github.jav:expo-server-sdk:1.1.0")
    implementation("com.opencsv", "opencsv", openCsvVersion)
    implementation("org.awaitility:awaitility")
    implementation("au.com.console:kotlin-jpa-specification-dsl:2.0.0")
    implementation("com.nimbusds:nimbus-jose-jwt:9.13")
    implementation("software.amazon.awssdk:cognitoidentityprovider:2.17.25")
    implementation("software.amazon.awssdk:s3:2.17.42")
    implementation("software.amazon.awssdk:sns:2.17.201")
    implementation("com.google.code.gson", "gson", gsonVersion)
    implementation("com.google.firebase:firebase-admin:9.1.1")
    implementation("commons-fileupload:commons-fileupload:1.5")
    implementation("commons-io:commons-io:2.15.1")
    implementation("com.algorand:algosdk:2.0.0")

    // implementation("com.fasterxml.jackson.core:jackson-core:2.13.4")
    // implementation("com.fasterxml.jackson.core:jackson-annotations:2.13.4")
    // implementation("com.fasterxml.jackson.core:jackson-databind:2.13.4")
    // implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.4")

    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.kotest", "kotest-assertions-core", kotestVersion)
    testImplementation("org.mockito", "mockito-core", mockitoVersion)
    testImplementation("io.mockk", "mockk", mockkVersion)
    testImplementation("com.ninja-squad", "springmockk", springmockkVersion)
    testImplementation("org.testcontainers", "testcontainers", testcontainersVersion)
    testImplementation("org.testcontainers", "junit-jupiter", testcontainersVersion)
    testImplementation("org.testcontainers", "postgresql", testcontainersVersion)

    runtimeOnly("org.postgresql:postgresql")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
