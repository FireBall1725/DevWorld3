// Unit tests and lint, applied to every version node
plugins {
    java
    checkstyle
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

checkstyle {
    toolVersion = "10.21.1"
    configDirectory.set(rootProject.file("config/checkstyle"))
    maxWarnings = 0
}
