import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/** What each version node tells the devworld-publish plugin about its jar. */
abstract class DevWorldPublishExtension {
    /** The jar to publish: the remapped one on Fabric, the reobfuscated one on Forge. */
    abstract val jar: RegularFileProperty

    /** Minecraft versions the jar runs on, for the CurseForge and Modrinth listings. */
    abstract val minecraftVersions: ListProperty<String>

    /** Whether the listing should declare Fabric API as a required dependency. */
    abstract val requiresFabricApi: Property<Boolean>
}
