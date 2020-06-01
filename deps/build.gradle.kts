plugins {
    id("java-library")
}

dependencies {
    api(project(":annotation"))
    api("com.discord4j:discord4j-core:3.1.0.RC2")
}