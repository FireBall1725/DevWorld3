// Maven artifacts for Jared's maven, and CurseForge and Modrinth uploads. Each node publishes its own jar.
import me.modmuss50.mpp.ReleaseType

plugins {
    `maven-publish`
    id("me.modmuss50.mod-publish-plugin")
}

val devworld = extensions.create<DevWorldPublishExtension>("devworldPublish")

val (minecraft, loader) = project.name.split('-', limit = 2)
val modVersion = property("mod.version") as String

// Jared's Jenkins sets local_maven to the folder its maven serves; there are no credentials on our side
val jaredMaven: String? = System.getenv("local_maven")
val publishForReal = System.getenv("PUBLISH_MODS") == "true"

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("mod") {
                groupId = property("mod.group") as String
                artifactId = "${property("mod.artifact")}-$loader-$minecraft"
                version = modVersion
                // Only the jar, so no Minecraft dependencies leak into the pom
                artifact(devworld.jar)
            }
        }
        repositories {
            if (jaredMaven != null) {
                maven {
                    name = "jared"
                    url = uri("file://$jaredMaven")
                }
            }
        }
    }

    publishMods {
        file.set(devworld.jar)
        displayName = "DevWorld 3 $modVersion ($loader $minecraft)"
        // One version per node, so the same number can sit next to itself on the listings
        version = "$modVersion+$minecraft-$loader"
        type = ReleaseType.BETA
        changelog = "DevWorld 3 $modVersion for $loader on Minecraft $minecraft."
        modLoaders.add(loader)
        // Nothing is uploaded unless PUBLISH_MODS=true
        dryRun = !publishForReal

        curseforge {
            projectId = property("mod.curse_project_id") as String
            accessToken = providers.environmentVariable("CURSEFORGE_TOKEN").orElse("dry-run")
            minecraftVersions.addAll(devworld.minecraftVersions)
            if (devworld.requiresFabricApi.getOrElse(false)) {
                requires("fabric-api")
            }
        }

        modrinth {
            projectId = property("mod.modrinth_project_id") as String
            accessToken = providers.environmentVariable("MODRINTH_TOKEN").orElse("dry-run")
            minecraftVersions.addAll(devworld.minecraftVersions)
            if (devworld.requiresFabricApi.getOrElse(false)) {
                requires("fabric-api")
            }
        }
    }
}
