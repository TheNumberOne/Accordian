plugins {
    java
    kotlin("jvm") version "1.3.72"
    kotlin("kapt") version "1.3.72"
}

dependencies {
    implementation("com.discord4j:discord4j-core:3.1.0.RC2")
    testImplementation(project(":deps"))
    kaptTest(project(":processor"))
    testImplementation(kotlin("stdlib-jdk8"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
}

repositories {
    mavenCentral()
}

tasks {
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    test {
        useJUnitPlatform()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}