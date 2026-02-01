plugins {
    id("gg.meza.stonecraft")
}

// Define loader from project name (e.g., "1.21.1-fabric" -> "fabric")
val projectName = project.name
val loader = when {
    projectName.contains("fabric") -> "fabric"
    projectName.contains("neoforge") -> "neoforge"
    projectName.contains("forge") -> "forge"
    else -> "forge" // default for root project
}

stonecutter {
    constants.match(loader, "fabric", "neoforge", "forge")
    constants["forgeLike"] = loader == "forge" || loader == "neoforge"
}

modSettings {
    runDirectory = rootProject.layout.projectDirectory.dir("run")
}

// Publishing configuration
publishMods {
    file = tasks.remapJar.get().archiveFile
    changelog = if (rootProject.file("CHANGELOG.md").exists()) {
        rootProject.file("CHANGELOG.md").readText()
    } else {
        "No changelog provided"
    }
    type = BETA
    modLoaders.add(loader)

    // Determine Minecraft version from project name
    val minecraftVersion = projectName.split("-").firstOrNull() ?: "unknown"

    dryRun = System.getenv("PUBLISH_MODS") == null

    // CurseForge
    if (System.getenv("CURSEFORGE_TOKEN") != null) {
        curseforge {
            accessToken = System.getenv("CURSEFORGE_TOKEN")
            projectId = property("curse_project_id").toString()
            minecraftVersions.add(minecraftVersion)

            // Add Java version requirement
            javaVersions.add(when (minecraftVersion) {
                "1.19.2" -> JavaVersion.VERSION_17
                "1.20.1" -> JavaVersion.VERSION_17
                "1.21.1" -> JavaVersion.VERSION_21
                else -> JavaVersion.VERSION_21
            })
        }
    }

    // Modrinth
    if (System.getenv("MODRINTH_TOKEN") != null) {
        modrinth {
            accessToken = System.getenv("MODRINTH_TOKEN")
            projectId = property("modrinth_project_id").toString()
            minecraftVersions.add(minecraftVersion)
        }
    }

    // GitHub Releases
    if (System.getenv("GITHUB_TOKEN") != null) {
        github {
            accessToken = System.getenv("GITHUB_TOKEN")
            repository = property("github_repository").toString()
            // Don't specify commitish - will use the tag's commit
        }
    }
}
