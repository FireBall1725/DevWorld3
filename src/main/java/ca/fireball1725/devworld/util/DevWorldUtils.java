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


    // Create the world
    public void createDevWorld() throws Exception {
        this.resultFolder = this.worldName.trim();
        Minecraft.getInstance().setScreen(null);
        queueLoadScreen(minecraft);

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



            WorldCreationContext finaltest = createFinalSettings(false, worldCreationContext);

            LevelSettings levelSettings = this.createLevelSettings();
            WorldData worldData = new PrimaryLevelData(levelSettings, finaltest.worldGenSettings(), finaltest.worldSettingsStability());
            this.minecraft.createWorldOpenFlows().createLevelFromExistingSettings(optional.get(), finaltest.dataPackResources(), finaltest.registryAccess(), worldData);
        }
    }

    public WorldCreationContext createFinalSettings(boolean isHardCore, WorldCreationContext worldCreationContext) {
        OptionalLong optionallong = WorldGenSettings.parseSeed("0");
        return worldCreationContext.withSettings((p_233071_) -> {
            return p_233071_.withSeed(isHardCore, optionallong);
        });
    }

    private void queueLoadScreen(Minecraft minecraft) {
        minecraft.forceSetScreen(new GenericDirtMessageScreen(DevWorldUtils.PREPARING_WORLD_DATA));
    }

    private WorldGenSettings flatWorldGenerator(RegistryAccess.Frozen registryaccess$frozen, WorldGenSettings worldGenSettings, FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        Registry<StructureSet> registry = registryaccess$frozen.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        ChunkGenerator chunkGenerator = new FlatLevelSource(registry, flatLevelGeneratorSettings);
        return WorldGenSettings.replaceOverworldGenerator(registryaccess$frozen, worldGenSettings, chunkGenerator);
    }

    private LevelSettings createLevelSettings() {
        String s = worldName.trim();
        GameRules gameRules = new GameRules();
        gameRules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
        gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
        return new LevelSettings(s, GameType.CREATIVE, false, Difficulty.NORMAL, true, gameRules, DataPackConfig.DEFAULT);
    }

    private static WorldLoader.InitConfig createDefaultLoadConfig(PackRepository p_232873_, DataPackConfig p_232874_) {
        WorldLoader.PackConfig worldloader$packconfig = new WorldLoader.PackConfig(p_232873_, p_232874_, false);
        return new WorldLoader.InitConfig(worldloader$packconfig, Commands.CommandSelection.INTEGRATED, 2);
    }

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
        this.popScreen();
        return Optional.empty();
    }

    private static void copyBetweenDirs(Path pFromDir, Path pToDir, Path pFilePath) {
        try {
            Util.copyBetweenDirs(pFromDir, pToDir, pFilePath);
        } catch (IOException ioexception) {
            LOGGER.warn("Failed to copy datapack file from {} to {}", pFilePath, pToDir);
            throw new UncheckedIOException(ioexception);
        }
    }

    public void popScreen() {
        // this.minecraft.setScreen(this.lastScreen);
        this.removeTempDataPackDir();
    }

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

    public boolean saveExist() {
        LevelStorageSource levelstoragesource = this.minecraft.getLevelSource();
        return levelstoragesource.levelExists(this.worldName);
    }

    public void loadDevWorld() {
        assert this.minecraft.screen != null;
        this.minecraft.createWorldOpenFlows().loadLevel(this.minecraft.screen, this.worldName);
    }

    public void deleteDevWorld() {
        LevelStorageSource levelstoragesource = this.minecraft.getLevelSource();

        try {
            LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = levelstoragesource.createAccess(this.worldName);

            try {
                levelstoragesource$levelstorageaccess.deleteLevel();
            } catch (Throwable throwable1) {
                try {
                    levelstoragesource$levelstorageaccess.close();
                } catch (Throwable throwable) {
                    throwable1.addSuppressed(throwable);
                }

                throw throwable1;
            }

            levelstoragesource$levelstorageaccess.close();
        } catch (IOException ioexception) {
            SystemToast.onWorldDeleteFailure(this.minecraft, this.worldName);
            LOGGER.error("Failed to delete world {}", this.worldName, ioexception);
        }
    }
}
