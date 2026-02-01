package ca.fireball1725.devworld.util;

import ca.fireball1725.devworld.config.Config;
import ca.fireball1725.devworld.config.DevWorldConfig;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.PresetFlatWorldScreen;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
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

/*? if <1.21 {*/
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Registry;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
/*?} else */
/*import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.flat.FlatLevelSource;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
/*?}*/

public class DevWorldUtils {
    private final String worldName = "DevWorld";
    private final Minecraft minecraft = Minecraft.getInstance();
    private final Component PREPARING_WORLD_DATA = Component.translatable("createWorld.preparing");
    @Nullable
    private Path tempDataPackDir;
    String resultFolder;
    private final Logger LOGGER = LogUtils.getLogger();

    public void createDevWorld() throws Exception {
        this.resultFolder = this.worldName.trim();
        Minecraft.getInstance().setScreen(null);
        queueLoadScreen();

        Optional<LevelStorageSource.LevelStorageAccess> optional = this.createNewWorldDirectory();
        if (optional.isPresent()) {
            this.removeTempDataPackDir();

            /*? if <1.21 {*/
            PackRepository packrepository = new PackRepository(PackType.SERVER_DATA, new ServerPacksSource());
            WorldLoader.InitConfig worldLoader$InitConfig = createDefaultLoadConfig(
                    packrepository,
                    new DataPackConfig(
                            ImmutableList.of("vanilla"),
                            ImmutableList.of()
                    )
            );

            CompletableFuture<WorldCreationContext> completableFuture = WorldLoader.load(
                    worldLoader$InitConfig,
                    (resourceManager, dataPackConfig) -> {
                        RegistryAccess.Frozen registryAccess$Frozen = RegistryAccess.builtinCopy().freeze();
                        WorldGenSettings worldgensettings = WorldPresets.createNormalWorldFromPreset(
                                registryAccess$Frozen,
                                0,
                                false,
                                DevWorldConfig.ENABLE_BONUS_CHEST.get()
                        );
                        return Pair.of(worldgensettings, registryAccess$Frozen);
                    },
                    (closeableResourceManager, datapackResources, registryAccess, levelSeed) -> {
                        closeableResourceManager.close();
                        return new WorldCreationContext(
                                levelSeed,
                                Lifecycle.stable(),
                                registryAccess,
                                datapackResources
                        );
                    },
                    Util.backgroundExecutor(),
                    this.minecraft
            );
            this.minecraft.managedBlock(completableFuture::isDone);

            WorldCreationContext worldCreationContext = completableFuture.get();
            FlatLevelGeneratorSettings flatLevelGeneratorSettings = PresetFlatWorldScreen.fromString(
                    worldCreationContext.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY),
                    worldCreationContext.registryAccess().registryOrThrow(Registry.STRUCTURE_SET_REGISTRY),
                    DevWorldConfig.FLATWORLD_GENERATOR_STRING.get(),
                    new FlatLevelGeneratorSettings(
                            Optional.empty(),
                            worldCreationContext.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY)
                    )
            );
            worldCreationContext = worldCreationContext.withSettings(
                    flatWorldGenerator(
                            worldCreationContext.registryAccess(),
                            worldCreationContext.worldGenSettings(),
                            flatLevelGeneratorSettings
                    )
            );
            WorldCreationContext finalWorldCreationContext = createFinalSettings(false, worldCreationContext);

            LevelSettings levelSettings = this.createLevelSettings();
            WorldData worldData = new PrimaryLevelData(
                    levelSettings,
                    finalWorldCreationContext.worldGenSettings(),
                    finalWorldCreationContext.worldSettingsStability()
            );
            this.minecraft.createWorldOpenFlows().createLevelFromExistingSettings(
                    optional.get(),
                    finalWorldCreationContext.dataPackResources(),
                    finalWorldCreationContext.registryAccess(),
                    worldData
            );
            /*?} else */
            /*// 1.21+ simplified world creation
            LevelSettings levelSettings = this.createLevelSettings();

            // Create default world options
            WorldOptions worldOptions = new WorldOptions(0L, false, DevWorldConfig.ENABLE_BONUS_CHEST.get());

            // Create flat level generator settings
            RegistryAccess.Frozen registryAccess = this.minecraft.getConnection() != null
                    ? this.minecraft.getConnection().registryAccess()
                    : RegistryAccess.fromRegistryOfRegistries(BuiltinRegistries.REGISTRY);

            // Create world data with flat world preset
            WorldData worldData = new PrimaryLevelData(
                    levelSettings,
                    worldOptions,
                    Lifecycle.stable()
            );

            this.minecraft.createWorldOpenFlows().createFreshLevel(
                    this.resultFolder,
                    levelSettings,
                    worldOptions,
                    (context) -> WorldDimensions.BUILTIN.get()
            );
            /*?}*/
        }
    }

    /*? if <1.21 {*/
    public WorldCreationContext createFinalSettings(boolean isHardCore, WorldCreationContext worldCreationContext) {
        OptionalLong optionallong = WorldGenSettings.parseSeed("0");
        return worldCreationContext.withSettings((worldSettings) -> {
            return worldSettings.withSeed(isHardCore, optionallong);
        });
    }

    private WorldGenSettings flatWorldGenerator(
            RegistryAccess.Frozen registryAccess$Frozen,
            WorldGenSettings worldGenSettings,
            FlatLevelGeneratorSettings flatLevelGeneratorSettings
    ) {
        Registry<StructureSet> registry = registryAccess$Frozen.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        ChunkGenerator chunkGenerator = new FlatLevelSource(registry, flatLevelGeneratorSettings);
        return WorldGenSettings.replaceOverworldGenerator(registryAccess$Frozen, worldGenSettings, chunkGenerator);
    }

    private WorldLoader.InitConfig createDefaultLoadConfig(
            PackRepository packRepository,
            DataPackConfig dataPackConfig
    ) {
        WorldLoader.PackConfig worldLoader$PackConfig = new WorldLoader.PackConfig(
                packRepository,
                dataPackConfig,
                false
        );
        return new WorldLoader.InitConfig(
                worldLoader$PackConfig,
                Commands.CommandSelection.INTEGRATED,
                2
        );
    }
    /*?}*/

    private void queueLoadScreen() {
        this.minecraft.forceSetScreen(new GenericDirtMessageScreen(this.PREPARING_WORLD_DATA));
    }

    private LevelSettings createLevelSettings() {
        String s = worldName.trim();
        GameRules gameRules = new GameRules();
        gameRules.getRule(GameRules.RULE_DAYLIGHT).set(DevWorldConfig.RULE_DAYLIGHT.get(), null);
        gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(DevWorldConfig.RULE_WEATHER_CYCLE.get(), null);
        gameRules.getRule(GameRules.RULE_DOFIRETICK).set(DevWorldConfig.RULE_DOFIRETICK.get(), null);
        gameRules.getRule(GameRules.RULE_MOBGRIEFING).set(DevWorldConfig.RULE_MOBGRIEFING.get(), null);
        gameRules.getRule(GameRules.RULE_DOMOBSPAWNING).set(DevWorldConfig.RULE_DOMOBSPAWNING.get(), null);
        gameRules.getRule(GameRules.RULE_DISABLE_RAIDS).set(DevWorldConfig.RULE_DISABLE_RAIDS.get(), null);
        gameRules.getRule(GameRules.RULE_DOINSOMNIA).set(DevWorldConfig.RULE_DOINSOMNIA.get(), null);

        /*? if <1.21 {*/
        return new LevelSettings(
                s,
                GameType.CREATIVE,
                false,
                Difficulty.NORMAL,
                true,
                gameRules,
                DataPackConfig.DEFAULT
        );
        /*?} else */
        /*return new LevelSettings(
                s,
                GameType.CREATIVE,
                false,
                Difficulty.NORMAL,
                true,
                gameRules,
                WorldDataConfiguration.DEFAULT
        );
        /*?}*/
    }

    private Optional<LevelStorageSource.LevelStorageAccess> createNewWorldDirectory() {
        try {
            LevelStorageSource.LevelStorageAccess levelStorageSource$LevelStorageAccess =
                    this.minecraft.getLevelSource().createAccess(this.resultFolder);

            if (this.tempDataPackDir == null) {
                return Optional.of(levelStorageSource$LevelStorageAccess);
            }

            try {
                Stream<Path> stream = Files.walk(this.tempDataPackDir);

                Optional optional;
                try {
                    Path path = levelStorageSource$LevelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR);
                    Files.createDirectories(path);
                    stream.filter((p_232921_) -> {
                        return !p_232921_.equals(this.tempDataPackDir);
                    }).forEach((p_232945_) -> {
                        copyBetweenDirs(this.tempDataPackDir, path, p_232945_);
                    });
                    optional = Optional.of(levelStorageSource$LevelStorageAccess);
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
                levelStorageSource$LevelStorageAccess.close();
            }
        } catch (UncheckedIOException | IOException ioexception1) {
            LOGGER.warn("Failed to create access for {}", this.resultFolder, ioexception1);
        }

        SystemToast.onPackCopyFailure(this.minecraft, this.resultFolder);
        this.removeTempDataPackDir();
        return Optional.empty();
    }

    private void copyBetweenDirs(Path pathFromDir, Path pathToDir, Path pathFilePath) {
        try {
            Util.copyBetweenDirs(pathFromDir, pathToDir, pathFilePath);
        } catch (IOException ioexception) {
            this.LOGGER.warn("Failed to copy datapack file from {} to {}", pathFilePath, pathToDir);
            throw new UncheckedIOException(ioexception);
        }
    }

    private void removeTempDataPackDir() {
        if (this.tempDataPackDir != null) {
            try {
                Stream<Path> stream = Files.walk(this.tempDataPackDir);

                try {
                    stream.sorted(Comparator.reverseOrder()).forEach((file) -> {
                        try {
                            Files.delete(file);
                        } catch (IOException ioexception1) {
                            LOGGER.warn("Failed to remove temporary file {}", file, ioexception1);
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

    public boolean saveExists() {
        LevelStorageSource levelstoragesource = this.minecraft.getLevelSource();
        return levelstoragesource.levelExists(this.worldName);
    }

    public void loadDevWorld() {
        assert this.minecraft.screen != null;
        this.minecraft.createWorldOpenFlows().loadLevel(this.minecraft.screen, this.worldName);
    }

    public void deleteDevWorld() {
        LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();

        try {
            LevelStorageSource.LevelStorageAccess levelStorageSource$LevelStorageAccess =
                    levelStorageSource.createAccess(this.worldName);

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
