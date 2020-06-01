plugins {
    id("java-library")
    kotlin("jvm") version "1.3.72"
    kotlin("kapt") version "1.3.72"
}

dependencies {
    api(project(":annotation"))

    implementation(kotlin("stdlib-jdk8"))

    compileOnly("com.google.auto.service:auto-service-annotations:1.0-rc7")
    kapt("com.google.auto.service:auto-service:1.0-rc7")
    compileOnly("net.ltgt.gradle.incap:incap:0.3")
    kapt("net.ltgt.gradle.incap:incap-processor:0.3")

    implementation("com.google.auto:auto-common:0.10")
    implementation("com.squareup:javapoet:1.12.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}