plugins {
    java
    kotlin("jvm") version "1.3.72"
}

dependencies {
    testImplementation(project(":annotation"))
    testImplementation(kotlin("stdlib-jdk8"))
}

repositories {
    mavenCentral()
}

tasks {
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}