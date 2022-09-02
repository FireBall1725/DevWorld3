package ca.fireball1725.devworld.util;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
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
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
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
        queueLoadScreen(minecraft, PREPARING_WORLD_DATA);

        Optional<LevelStorageSource.LevelStorageAccess> optional = this.createNewWorldDirectory();
        if (!optional.isEmpty()) {
            this.removeTempDataPackDir();


            HolderSet<StructureSet> holderset = HolderSet.direct(BuiltinRegistries.STRUCTURE_SETS.getHolderOrThrow(BuiltinStructureSets.STRONGHOLDS), BuiltinRegistries.STRUCTURE_SETS.getHolderOrThrow(BuiltinStructureSets.VILLAGES));

            FlatLevelGeneratorSettings flatlevelgeneratorsettings = new FlatLevelGeneratorSettings(Optional.of(holderset), BuiltinRegistries.BIOME);
            flatlevelgeneratorsettings.setBiome(BuiltinRegistries.BIOME.getOrCreateHolderOrThrow(Biomes.DESERT));
            flatlevelgeneratorsettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
            flatlevelgeneratorsettings.getLayersInfo().add(new FlatLayerInfo(3, Blocks.COBBLESTONE));
            flatlevelgeneratorsettings.getLayersInfo().add(new FlatLayerInfo(130, Blocks.SANDSTONE));
            flatlevelgeneratorsettings.updateLayers();


            PackRepository packrepository = new PackRepository(PackType.SERVER_DATA, new ServerPacksSource());
            WorldLoader.InitConfig worldloader$initconfig = createDefaultLoadConfig(packrepository, new DataPackConfig(ImmutableList.of("vanilla"), ImmutableList.of()));


            // FlatGenerationSettings flatGenerationSettings = FlatGenerationSettings.createFlatGeneratorFromString("minecraft:bedrock,3*minecraft:stone,52*minecraft:sandstone;minecraft:desert;");

            CompletableFuture<WorldCreationContext> completablefuture = WorldLoader.load(worldloader$initconfig, (resourceManager, dataPackConfig) -> {
                RegistryAccess.Frozen registryaccess$frozen = RegistryAccess.builtinCopy().freeze();
                WorldGenSettings worldgensettings = WorldPresets.createNormalWorldFromPreset(registryaccess$frozen, 0, true, false);
                ChunkGenerator chunkgenerator = new FlatLevelSource(BuiltinRegistries.STRUCTURE_SETS, flatlevelgeneratorsettings);

                worldgensettings = WorldGenSettings.replaceOverworldGenerator(registryaccess$frozen, worldgensettings, chunkgenerator);


                //worldgensettings = flatWorldConfigurator(flatlevelgeneratorsettings).apply(registryaccess$frozen, worldgensettings);
                return Pair.of(worldgensettings, registryaccess$frozen);
            }, (closeableResourceManager, datapackResources, registryAccess, levelSeed) -> {
                closeableResourceManager.close();
                return new WorldCreationContext(levelSeed, Lifecycle.stable(), registryAccess, datapackResources);
            }, Util.backgroundExecutor(), this.minecraft);

            this.minecraft.managedBlock(completablefuture::isDone);

            WorldCreationContext worldCreationContext = completablefuture.get();



            //WorldCreationContext worldCreationContext = new WorldGenSettingsComponent(completablefuture.join(), Optional.of(WorldPresets.FLAT), OptionalLong.empty()).createFinalSettings(false);

            WorldGenSettings worldGenSettings = worldCreationContext.worldGenSettings();







            LevelSettings levelSettings = this.createLevelSettings();
            WorldData worldData = new PrimaryLevelData(levelSettings, worldGenSettings, worldCreationContext.worldSettingsStability());

            this.minecraft.createWorldOpenFlows().createLevelFromExistingSettings(optional.get(), worldCreationContext.dataPackResources(), worldCreationContext.registryAccess(), worldData);
        }
    }

    private void queueLoadScreen(Minecraft minecraft, Component component) {
        minecraft.forceSetScreen(new GenericDirtMessageScreen(component));
    }

    private LevelSettings createLevelSettings() {
        String s = worldName.trim();
        GameRules gameRules = new GameRules();
        gameRules.getRule(GameRules.RULE_DAYLIGHT).set(false, (MinecraftServer)null);
        gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, (MinecraftServer)null);
        return new LevelSettings(s, GameType.CREATIVE, false, Difficulty.NORMAL, true, gameRules, DataPackConfig.DEFAULT);
    }

    private static WorldCreationContext.Updater flatWorldConfigurator(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        return (registryAccess, worldGenSettings) -> {
            Registry<StructureSet> registry = registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
            ChunkGenerator chunkgenerator = new FlatLevelSource(registry, flatLevelGeneratorSettings);
            return WorldGenSettings.replaceOverworldGenerator(registryAccess, worldGenSettings, chunkgenerator);
        };
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

                if (stream != null) {
                    stream.close();
                }

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

                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ioexception) {
                LOGGER.warn("Failed to list temporary dir {}", (Object)this.tempDataPackDir);
            }

            this.tempDataPackDir = null;
        }

    }
}
