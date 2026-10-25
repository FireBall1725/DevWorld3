plugins {
    id("net.neoforged.moddev.legacyforge") version "2.0.147"
    id("neoforge-mutex")
    id("devworld-quality")
    id("devworld-publish")
}

version = property("mod.version") as String
base.archivesName = "${property("mod.artifact") as String}-forge-${sc.current.version}"

val modId = property("mod.id") as String
val runDir = rootProject.file("run/${project.name}")


// The Minecraft versions this jar runs on, for the CurseForge and Modrinth listings
val compatibleVersions: List<String> = sc.properties.rawOrNull("mod", "mc_releases")
    ?.asList().orEmpty().map { it.toString() }

val requiredJava = when {
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
}

legacyForge {
    version = property("deps.forge") as String

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        register("client") {
            gameDirectory = runDir
            client()
        }
    }
}

// The legacy plugin has no unit test support; give the tests the same Minecraft classpath as the main code
sourceSets.test {
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
}

tasks.test {
    // Remapping Forge breaks its jar signatures, so a test that loads Minecraft classes dies with
    // "SHA-384 digest error" on some runs. That test runs on the Fabric and NeoForge nodes.
    filter.excludeTestsMatching("*GameRuleApplierTest")
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
    jar.fileProvider(tasks.named("reobfJar").map { it.outputs.files.singleFile })
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
            register("pack_format", sc.properties["mod.pack_format"])
            register("forge_major", sc.properties["mod.forge_major"])
            register("description", sc.properties["mod.description"])
        }

        filesMatching("META-INF/mods.toml") { expand(props) }
        filesMatching("pack.mcmeta") { expand(props) }

        exclude("fabric.mod.json", "META-INF/neoforge.mods.toml")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    withType<Jar> {
        val name = project.property("mod.id")
        inputs.property("mod_id", name)
        from("../../LICENSE") { rename { "$it-$name" } }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        from(named("reobfJar"), named<Jar>("sourcesJar").flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}
