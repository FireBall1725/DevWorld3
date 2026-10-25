plugins {
    id("net.neoforged.moddev") version "2.0.147"
    id("neoforge-mutex")
    id("devworld-quality")
    id("devworld-publish")
}

version = property("mod.version") as String
base.archivesName = "${property("mod.artifact") as String}-neoforge-${project.name.substringBefore('-')}"


// The Minecraft versions this jar runs on, for the CurseForge and Modrinth listings
val compatibleVersions: List<String> = sc.properties.rawOrNull("mod", "mc_releases")
    ?.asList().orEmpty().map { it.toString() }

val requiredJava = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

val runDir = rootProject.file("run/${project.name}")

neoForge {
    version = property("deps.neo_loader") as String

    mods {
        register(property("mod.id") as String) {
            sourceSet(sourceSets.main.get())
        }
    }

    unitTest {
        enable()
        testedMod = mods.getByName(property("mod.id") as String)
    }

    runs {
        register("client") {
            gameDirectory = runDir
            client()
        }

        register("server") {
            gameDirectory = runDir
            server()
        }
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

devworldPublish {
    jar.set(tasks.jar.flatMap { it.archiveFile })
    minecraftVersions.addAll(compatibleVersions)
}

tasks {
    processResources {
        fun MutableMap<String, String>.register(key: String, value: String) {
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", sc.properties["mod.id"])
            register("name", sc.properties["mod.name"])
            register("version", sc.properties["mod.version"])
            register("minecraft", sc.properties["mod.mc_compat"])
            register("authors", sc.properties["mod.authors"])
            register("pack_fields", sc.properties["mod.pack_fields"])
            // NeoForge 26.2 warns on startup about the old key
            register("logo_key", if (sc.current.parsed >= "26.2") "iconFile" else "logoFile")
            register("description", sc.properties["mod.description"])
        }

        filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
        filesMatching("pack.mcmeta") { expand(props) }

        exclude("fabric.mod.json", "META-INF/mods.toml")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    // Includes the license file in the built mod
    withType<Jar> {
        val name = project.property("mod.id")
        inputs.property("mod_id", name)
        from("../../LICENSE") { rename { "$it-$name" } }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        from(jar.flatMap { it.archiveFile }, named<Jar>("sourcesJar").flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}
