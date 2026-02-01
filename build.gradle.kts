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
// TODO: Re-enable publishing after build works
// publishMods {
//     changelog = if (rootProject.file("CHANGELOG.md").exists()) {
//         rootProject.file("CHANGELOG.md").readText()
//     } else {
//         "No changelog provided"
//     }
//     type = BETA
//     dryRun = System.getenv("PUBLISH_MODS") == null
// }
