package ca.fireball1725.devworld.util;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.PresetFlatWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class DevWorldUtils {
    private final String worldName = "DevWorld";
    private final Minecraft minecraft = Minecraft.getInstance();
    private static final Component PREPARING_WORLD_DATA = Component.translatable("createWorld.preparing");
    @Nullable
    private Path tempDataPackDir;
    String resultFolder;
    private static final Logger LOGGER = LogUtils.getLogger();


    /**
     * Create a new dev world
     * @throws Exception if there is an issue with the save
     */
    public void createDevWorld() throws Exception {
        this.resultFolder = this.worldName.trim();
        Minecraft.getInstance().setScreen(null);
        queueLoadScreen();

        Optional<LevelStorageSource.LevelStorageAccess> optional = this.createNewWorldDirectory();
        if (optional.isPresent()) {
            this.removeTempDataPackDir();

            PackRepository packrepository = new PackRepository(PackType.SERVER_DATA, new ServerPacksSource());
            WorldLoader.InitConfig worldloader$initconfig = createDefaultLoadConfig(packrepository, new DataPackConfig(ImmutableList.of("vanilla"), ImmutableList.of()));

            CompletableFuture<WorldCreationContext> completableFuture = WorldLoader.load(worldloader$initconfig, (resourceManager, dataPackConfig) -> {
                RegistryAccess.Frozen registryaccess$frozen = RegistryAccess.builtinCopy().freeze();
                WorldGenSettings worldgensettings = WorldPresets.createNormalWorldFromPreset(registryaccess$frozen, 0, false, false);
                return Pair.of(worldgensettings, registryaccess$frozen);
            }, (closeableResourceManager, datapackResources, registryAccess, levelSeed) -> {
                closeableResourceManager.close();
                return new WorldCreationContext(levelSeed, Lifecycle.stable(), registryAccess, datapackResources);
            }, Util.backgroundExecutor(), this.minecraft);
            this.minecraft.managedBlock(completableFuture::isDone);

            WorldCreationContext worldCreationContext = completableFuture.get();
            FlatLevelGeneratorSettings flatLevelGeneratorSettings = PresetFlatWorldScreen.fromString(worldCreationContext.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), worldCreationContext.registryAccess().registryOrThrow(Registry.STRUCTURE_SET_REGISTRY), "minecraft:bedrock,3*minecraft:stone,130*minecraft:sandstone;minecraft:desert;", new FlatLevelGeneratorSettings(Optional.empty(), worldCreationContext.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY)));
            worldCreationContext = worldCreationContext.withSettings(flatWorldGenerator(worldCreationContext.registryAccess(), worldCreationContext.worldGenSettings(), flatLevelGeneratorSettings));
            WorldCreationContext finalWorldCreationContext = createFinalSettings(false, worldCreationContext);

            LevelSettings levelSettings = this.createLevelSettings();
            WorldData worldData = new PrimaryLevelData(levelSettings, finalWorldCreationContext.worldGenSettings(), finalWorldCreationContext.worldSettingsStability());
            this.minecraft.createWorldOpenFlows().createLevelFromExistingSettings(optional.get(), finalWorldCreationContext.dataPackResources(), finalWorldCreationContext.registryAccess(), worldData);
        }
    }

    /**
     *
     * @param isHardCore
     * @param worldCreationContext
     * @return
     */
    public WorldCreationContext createFinalSettings(boolean isHardCore, WorldCreationContext worldCreationContext) {
        OptionalLong optionallong = WorldGenSettings.parseSeed("0");
        return worldCreationContext.withSettings((p_233071_) -> {
            return p_233071_.withSeed(isHardCore, optionallong);
        });
    }

    /**
     * Show loading screen
     */
    private void queueLoadScreen() {
        this.minecraft.forceSetScreen(new GenericDirtMessageScreen(DevWorldUtils.PREPARING_WORLD_DATA));
    }

    /**
     * Flat world generator
     * @param registryAccess$Frozen frozen registry access
     * @param worldGenSettings world generator settings
     * @param flatLevelGeneratorSettings flat level generator settings
     * @return world generator settings with overworld replaced with the flat world generator
     */
    private WorldGenSettings flatWorldGenerator(RegistryAccess.Frozen registryAccess$Frozen, WorldGenSettings worldGenSettings, FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        Registry<StructureSet> registry = registryAccess$Frozen.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        ChunkGenerator chunkGenerator = new FlatLevelSource(registry, flatLevelGeneratorSettings);
        return WorldGenSettings.replaceOverworldGenerator(registryAccess$Frozen, worldGenSettings, chunkGenerator);
    }

    /**
     * Create the level settings
     * @return the level settings
     */
    private LevelSettings createLevelSettings() {
        String s = worldName.trim();
        GameRules gameRules = new GameRules();
        gameRules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
        gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
        return new LevelSettings(s, GameType.CREATIVE, false, Difficulty.NORMAL, true, gameRules, DataPackConfig.DEFAULT);
    }

    /**
     * Create default load configuration
     * @param p_232873_
     * @param p_232874_
     * @return
     */
    private static WorldLoader.InitConfig createDefaultLoadConfig(PackRepository p_232873_, DataPackConfig p_232874_) {
        WorldLoader.PackConfig worldloader$packconfig = new WorldLoader.PackConfig(p_232873_, p_232874_, false);
        return new WorldLoader.InitConfig(worldloader$packconfig, Commands.CommandSelection.INTEGRATED, 2);
    }

    /**
     * Create new world directory
     * @return Level storage source
     */
    private Optional<LevelStorageSource.LevelStorageAccess> createNewWorldDirectory() {
        try {
            LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = this.minecraft.getLevelSource().createAccess(this.resultFolder);

            if (this.tempDataPackDir == null) {
                return Optional.of(levelstoragesource$levelstorageaccess);
            }

            try {
                Stream<Path> stream = Files.walk(this.tempDataPackDir);

                Optional optional;
                try {
                    Path path = levelstoragesource$levelstorageaccess.getLevelPath(LevelResource.DATAPACK_DIR);
                    Files.createDirectories(path);
                    stream.filter((p_232921_) -> {
                        return !p_232921_.equals(this.tempDataPackDir);
                    }).forEach((p_232945_) -> {
                        copyBetweenDirs(this.tempDataPackDir, path, p_232945_);
                    });
                    optional = Optional.of(levelstoragesource$levelstorageaccess);
                } catch (Throwable throwable1) {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (Throwable throwable) {
                            throwable1.addSuppressed(throwable);
                        }
                    }

                    throw throwable1;
                }

                stream.close();

                return optional;
            } catch (UncheckedIOException | IOException ioexception) {
                LOGGER.warn("Failed to copy datapacks to world {}", this.resultFolder, ioexception);
                levelstoragesource$levelstorageaccess.close();
            }
        } catch (UncheckedIOException | IOException ioexception1) {
            LOGGER.warn("Failed to create access for {}", this.resultFolder, ioexception1);
        }

        SystemToast.onPackCopyFailure(this.minecraft, this.resultFolder);
        this.removeTempDataPackDir();
        return Optional.empty();
    }

    /**
     * Copy between directories
     * @param pathFromDir directory to copy from
     * @param pathToDir directory to copy to
     * @param pathFilePath path of where files are
     */
    private static void copyBetweenDirs(Path pathFromDir, Path pathToDir, Path pathFilePath) {
        try {
            Util.copyBetweenDirs(pathFromDir, pathToDir, pathFilePath);
        } catch (IOException ioexception) {
            LOGGER.warn("Failed to copy datapack file from {} to {}", pathFilePath, pathToDir);
            throw new UncheckedIOException(ioexception);
        }
    }

    /**
     * Remove the temporary data pack directory
     */
    private void removeTempDataPackDir() {
        if (this.tempDataPackDir != null) {
            try {
                Stream<Path> stream = Files.walk(this.tempDataPackDir);

                try {
                    stream.sorted(Comparator.reverseOrder()).forEach((p_232942_) -> {
                        try {
                            Files.delete(p_232942_);
                        } catch (IOException ioexception1) {
                            LOGGER.warn("Failed to remove temporary file {}", p_232942_, ioexception1);
                        }

                    });
                } catch (Throwable throwable1) {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (Throwable throwable) {
                            throwable1.addSuppressed(throwable);
                        }
                    }

                    throw throwable1;
                }

                stream.close();
            } catch (IOException ioexception) {
                LOGGER.warn("Failed to list temporary dir {}", (Object)this.tempDataPackDir);
            }

            this.tempDataPackDir = null;
        }

    }

    /**
     * Check to see if there is a dev world save
     * @return boolean of if the save exists
     */
    public boolean saveExists() {
        LevelStorageSource levelstoragesource = this.minecraft.getLevelSource();
        return levelstoragesource.levelExists(this.worldName);
    }

    /**
     * Load the dev world
     */
    public void loadDevWorld() {
        assert this.minecraft.screen != null;
        this.minecraft.createWorldOpenFlows().loadLevel(this.minecraft.screen, this.worldName);
    }

    /**
     * Delete the dev world
     */
    public void deleteDevWorld() {
        LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();

        try {
            LevelStorageSource.LevelStorageAccess levelStorageSource$LevelStorageAccess = levelStorageSource.createAccess(this.worldName);

            try {
                levelStorageSource$LevelStorageAccess.deleteLevel();
            } catch (Throwable throwable1) {
                try {
                    levelStorageSource$LevelStorageAccess.close();
                } catch (Throwable throwable) {
                    throwable1.addSuppressed(throwable);
                }

                throw throwable1;
            }

            levelStorageSource$LevelStorageAccess.close();
        } catch (IOException ioexception) {
            SystemToast.onWorldDeleteFailure(this.minecraft, this.worldName);
            LOGGER.error("Failed to delete world {}", this.worldName, ioexception);
        }
    }
}
