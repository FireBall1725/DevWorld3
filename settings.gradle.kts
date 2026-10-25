pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/") { name = "FabricMC" }
    maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
    maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
    maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
  }
}

plugins {
  id("dev.kikugie.stonecutter") version "0.9.8"
  // Picks the right Loom variant per Minecraft version
  id("dev.kikugie.loom-back-compat") version "0.4.2"
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
  create(rootProject) {
    // Creates `versions/{project}-{loader}` nodes, each with its own `build.{loader}.gradle.kts`.
    fun match(project: String, vararg loaders: String, version: String = project) {
      for (loader in loaders) version("$project-$loader", version).buildscript("build.$loader.gradle.kts")
    }

    match("1.19.2", "fabric", "forge")
    match("1.20.1", "fabric", "forge")
    match("1.21.1", "fabric", "neoforge")
    vcsVersion = "1.21.1-fabric"
  }
}

rootProject.name = "DevWorld3"
