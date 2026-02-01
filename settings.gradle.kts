pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.parchmentmc.org")
    }
}

plugins {
    id("gg.meza.stonecraft") version "1.+"
    id("dev.kikugie.stonecutter") version "0.8+"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        version("1.21.1-fabric", "1.21.1")
        version("1.21.1-neoforge", "1.21.1")
        version("1.20.1-forge", "1.20.1")
        version("1.19.2-forge", "1.19.2")
    }
}

rootProject.name = "DevWorld3"
