plugins {
    id("com.github.ben-manes.versions") version "0.28.0"
}

allprojects {
    group = "accordion"
    version = "0.0.1-SNAPSHOT"
    apply(plugin = "com.github.ben-manes.versions")

    repositories {
        mavenCentral()
    }
}