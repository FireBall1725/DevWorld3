package ca.fireball1725.devworld.util;

import ca.fireball1725.devworld.config.Config;
import ca.fireball1725.devworld.config.DevWorldConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import org.slf4j.Logger;

import java.io.IOException;

/*? if <1.21 {*/
/*import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import net.minecraft.Util;
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
import org.jetbrains.annotations.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
*//*?} else {*/
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/*?}*/

public class DevWorldUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String worldName = "DevWorld";
    private final Minecraft minecraft = Minecraft.getInstance();

    public DevWorldUtils() {
    }

    /*? if <1.21 {*/
    /*public void createDevWorld() throws Exception {
        // 1.19.2 and 1.20.1: Temporarily disable custom world creation
        // TODO: Implement proper flat world creation for older versions
        throw new UnsupportedOperationException(
            "World creation for MC <1.21 is not yet implemented in the Stonecutter migration. " +
            "The Minecraft world creation API changed significantly between versions.");
    }
    *//*?} else {*/
    /**
     * Create a new dev world with configured settings from the config file.
     * Programmatically creates a flat world with configured game rules.
     * @throws Exception if there is an issue with world creation or save
     */
    public void createDevWorld() throws Exception {
        LOGGER.info("Creating DevWorld with custom settings");

        // Schedule world creation on the main thread to ensure proper context
        minecraft.execute(() -> {
            try {
                createDevWorldInternal();
            } catch (Exception e) {
                LOGGER.error("Failed to create DevWorld", e);
                SystemToast.onWorldAccessFailure(minecraft, worldName);
            }
        });
    }

    /**
     * Internal method that performs the actual world creation.
     * Must be called from the main Minecraft thread.
     */
    private void createDevWorldInternal() throws Exception {
        // Get the current screen to pass through
        Screen currentScreen = minecraft.screen;

        // Create game rules from config
        GameRules gameRules = createGameRules();

        // Create world data configuration with vanilla features only (no experimental)
        WorldDataConfiguration worldDataConfig = new WorldDataConfiguration(
                net.minecraft.world.level.DataPackConfig.DEFAULT,
                net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS
        );

        LevelSettings levelSettings = new LevelSettings(
                worldName,
                GameType.CREATIVE,
                false, // hardcore
                Difficulty.PEACEFUL,
                true, // allowCommands
                gameRules,
                worldDataConfig
        );

        // Create world options
        WorldOptions worldOptions = new WorldOptions(
                0L, // seed (0 = random)
                false, // generateStructures
                getEnableBonusChest()
        );

        // Create the world using Minecraft's world creation flow
        minecraft.createWorldOpenFlows().createFreshLevel(
                worldName,
                levelSettings,
                worldOptions,
                this::createFlatWorldDimensions,
                currentScreen
        );
    }

    /**
     * Creates game rules based on config settings.
     */
    private GameRules createGameRules() {
        GameRules gameRules = new GameRules();

        gameRules.getRule(GameRules.RULE_DAYLIGHT).set(getRuleDaylight(), null);
        gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(getRuleWeatherCycle(), null);
        gameRules.getRule(GameRules.RULE_DOFIRETICK).set(getRuleDoFireTick(), null);
        gameRules.getRule(GameRules.RULE_MOBGRIEFING).set(getRuleMobGriefing(), null);
        gameRules.getRule(GameRules.RULE_DOMOBSPAWNING).set(getRuleDoMobSpawning(), null);
        gameRules.getRule(GameRules.RULE_DISABLE_RAIDS).set(getRuleDisableRaids(), null);
        gameRules.getRule(GameRules.RULE_DOINSOMNIA).set(getRuleDoInsomnia(), null);

        return gameRules;
    }

    /**
     * Creates flat world dimensions from custom preset string or falls back to default.
     */
    private WorldDimensions createFlatWorldDimensions(RegistryAccess registryAccess) {
        String presetString = getFlatworldGeneratorString();

        if (presetString == null || presetString.trim().isEmpty()) {
            LOGGER.info("No custom preset configured, using default flat preset");
            return createDefaultFlatWorld(registryAccess);
        }

        try {
            LOGGER.info("Parsing custom flat world preset: {}", presetString);
            FlatLevelGeneratorSettings settings = parsePresetString(presetString, registryAccess);
            return createWorldDimensionsFromFlatSettings(settings, registryAccess);
        } catch (Exception e) {
            LOGGER.error("Failed to parse flat world preset '{}', falling back to default", presetString, e);
            return createDefaultFlatWorld(registryAccess);
        }
    }

    private WorldDimensions createDefaultFlatWorld(RegistryAccess registryAccess) {
        return registryAccess.lookupOrThrow(Registries.WORLD_PRESET)
                .getOrThrow(WorldPresets.FLAT)
                .value()
                .createWorldDimensions();
    }

    private FlatLevelGeneratorSettings parsePresetString(String presetString, RegistryAccess registryAccess) {
        String[] parts = presetString.split(";", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Preset must contain layers and biome separated by ';'");
        }

        String layersString = parts[0].trim();
        String biomeString = parts[1].trim();

        List<FlatLayerInfo> layers = parseLayerString(layersString, registryAccess);
        Holder<Biome> biome = parseBiome(biomeString, registryAccess);

        HolderGetter<Biome> biomeRegistry = registryAccess.lookupOrThrow(Registries.BIOME);
        HolderGetter<StructureSet> structureSetRegistry = registryAccess.lookupOrThrow(Registries.STRUCTURE_SET);
        HolderGetter<PlacedFeature> placedFeatureRegistry = registryAccess.lookupOrThrow(Registries.PLACED_FEATURE);

        FlatLevelGeneratorSettings defaultSettings = FlatLevelGeneratorSettings.getDefault(
                biomeRegistry,
                structureSetRegistry,
                placedFeatureRegistry
        );

        return defaultSettings.withBiomeAndLayers(
                layers,
                defaultSettings.structureOverrides(),
                biome
        );
    }

    private List<FlatLayerInfo> parseLayerString(String layersString, RegistryAccess registryAccess) {
        if (layersString.isEmpty()) {
            throw new IllegalArgumentException("Layers string cannot be empty");
        }

        HolderGetter<Block> blockRegistry = registryAccess.lookupOrThrow(Registries.BLOCK);
        List<FlatLayerInfo> layers = new ArrayList<>();

        String[] layerParts = layersString.split(",");
        for (String layerPart : layerParts) {
            layerPart = layerPart.trim();
            if (layerPart.isEmpty()) continue;

            int count = 1;
            String blockId;

            if (layerPart.contains("*")) {
                String[] countAndBlock = layerPart.split("\\*", 2);
                try {
                    count = Integer.parseInt(countAndBlock[0].trim());
                    blockId = countAndBlock[1].trim();
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid layer count in: " + layerPart, e);
                }
            } else {
                blockId = layerPart;
            }

            ResourceLocation blockResourceLocation;
            try {
                blockResourceLocation = ResourceLocation.parse(blockId);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid block ID: " + blockId, e);
            }

            ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, blockResourceLocation);
            Optional<Holder.Reference<Block>> blockHolder = blockRegistry.get(blockKey);

            if (blockHolder.isEmpty()) {
                throw new IllegalArgumentException("Unknown block: " + blockId);
            }

            Block block = blockHolder.get().value();
            layers.add(new FlatLayerInfo(count, block));
        }

        if (layers.isEmpty()) {
            throw new IllegalArgumentException("No valid layers found in preset");
        }

        return layers;
    }

    private Holder<Biome> parseBiome(String biomeString, RegistryAccess registryAccess) {
        if (biomeString.isEmpty()) {
            throw new IllegalArgumentException("Biome string cannot be empty");
        }

        ResourceLocation biomeResourceLocation;
        try {
            biomeResourceLocation = ResourceLocation.parse(biomeString);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid biome ID: " + biomeString, e);
        }

        HolderGetter<Biome> biomeRegistry = registryAccess.lookupOrThrow(Registries.BIOME);
        ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, biomeResourceLocation);
        Optional<Holder.Reference<Biome>> biomeHolder = biomeRegistry.get(biomeKey);

        if (biomeHolder.isEmpty()) {
            LOGGER.warn("Unknown biome '{}', falling back to plains", biomeString);
            return biomeRegistry.getOrThrow(Biomes.PLAINS);
        }

        return biomeHolder.get();
    }

    private WorldDimensions createWorldDimensionsFromFlatSettings(
            FlatLevelGeneratorSettings settings,
            RegistryAccess registryAccess) {

        net.minecraft.world.level.levelgen.presets.WorldPreset flatPreset =
                registryAccess.lookupOrThrow(Registries.WORLD_PRESET)
                        .getOrThrow(WorldPresets.FLAT)
                        .value();

        WorldDimensions baseDimensions = flatPreset.createWorldDimensions();

        net.minecraft.world.level.chunk.ChunkGenerator customOverworldGenerator =
                new net.minecraft.world.level.levelgen.FlatLevelSource(settings);

        HolderGetter<net.minecraft.world.level.dimension.DimensionType> dimensionTypes =
                registryAccess.lookupOrThrow(Registries.DIMENSION_TYPE);
        Holder<net.minecraft.world.level.dimension.DimensionType> overworldType =
                dimensionTypes.getOrThrow(net.minecraft.world.level.dimension.BuiltinDimensionTypes.OVERWORLD);

        LevelStem customOverworldStem = new LevelStem(overworldType, customOverworldGenerator);

        net.minecraft.core.MappedRegistry<LevelStem> levelStemRegistry =
                new net.minecraft.core.MappedRegistry<>(Registries.LEVEL_STEM, com.mojang.serialization.Lifecycle.stable());

        levelStemRegistry.register(
                ResourceKey.create(Registries.LEVEL_STEM, Level.OVERWORLD.location()),
                customOverworldStem,
                net.minecraft.core.RegistrationInfo.BUILT_IN
        );

        baseDimensions.dimensions().entrySet().stream()
                .filter(entry -> !entry.getKey().location().equals(Level.OVERWORLD.location()))
                .forEach(entry -> levelStemRegistry.register(
                        entry.getKey(),
                        entry.getValue(),
                        net.minecraft.core.RegistrationInfo.BUILT_IN
                ));

        return new WorldDimensions(levelStemRegistry);
    }
    /*?}*/

    /**
     * Check to see if there is a dev world save
     */
    public boolean saveExists() {
        LevelStorageSource levelstoragesource = this.minecraft.getLevelSource();
        return levelstoragesource.levelExists(this.worldName);
    }

    /**
     * Load the dev world
     */
    public void loadDevWorld() {
        /*? if <1.21 {*/
        /*this.minecraft.forceSetScreen(null);
        this.minecraft.createWorldOpenFlows().loadLevel(this.minecraft.screen, this.worldName);
        *//*?} else {*/
        this.minecraft.createWorldOpenFlows().openWorld(this.worldName, () -> minecraft.setScreen(null));
        /*?}*/
    }

    /**
     * Delete the dev world
     */
    public void deleteDevWorld() {
        LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();

        try {
            LevelStorageSource.LevelStorageAccess levelStorageAccess =
                    levelStorageSource.createAccess(this.worldName);

            try {
                levelStorageAccess.deleteLevel();
            } catch (Throwable throwable1) {
                try {
                    levelStorageAccess.close();
                } catch (Throwable throwable) {
                    throwable1.addSuppressed(throwable);
                }
                throw throwable1;
            }

            levelStorageAccess.close();
        } catch (IOException ioexception) {
            SystemToast.onWorldDeleteFailure(this.minecraft, this.worldName);
            LOGGER.error("Failed to delete world {}", this.worldName, ioexception);
        }
    }

    // Config accessors - adapt static config to instance methods
    private boolean getEnableBonusChest() {
        /*? if forge {*/
        /*return DevWorldConfig.ENABLE_BONUS_CHEST.get();
        *//*?} elif neoforge {*/
        return DevWorldConfig.ENABLE_BONUS_CHEST.get();
        /*?} else {*/
        /*return Config.EXTERNAL_CONFIG.worldConfig.bonusChest != null ?
            Config.EXTERNAL_CONFIG.worldConfig.bonusChest : false;
        *//*?}*/
    }

    private String getFlatworldGeneratorString() {
        /*? if forge {*/
        /*return DevWorldConfig.FLATWORLD_GENERATOR_STRING.get();
        *//*?} elif neoforge {*/
        return DevWorldConfig.FLATWORLD_GENERATOR_STRING.get();
        /*?} else {*/
        /*return Config.EXTERNAL_CONFIG.worldConfig.worldGenerationPreset;
        *//*?}*/
    }

    private boolean getRuleDaylight() {
        /*? if forge {*/
        /*return DevWorldConfig.RULE_DAYLIGHT.get();
        *//*?} elif neoforge {*/
        return DevWorldConfig.RULE_DAYLIGHT.get();
        /*?} else {*/
        /*return Config.EXTERNAL_CONFIG.gameRulesConfig.ruleDayLightCycle != null ?
            Config.EXTERNAL_CONFIG.gameRulesConfig.ruleDayLightCycle : false;
        *//*?}*/
    }

    private boolean getRuleWeatherCycle() {
        /*? if forge {*/
        /*return DevWorldConfig.RULE_WEATHER_CYCLE.get();
        *//*?} elif neoforge {*/
        return DevWorldConfig.RULE_WEATHER_CYCLE.get();
        /*?} else {*/
        /*return Config.EXTERNAL_CONFIG.gameRulesConfig.ruleWeatherCycle != null ?
            Config.EXTERNAL_CONFIG.gameRulesConfig.ruleWeatherCycle : false;
        *//*?}*/
    }

    private boolean getRuleDoFireTick() {
        /*? if forge {*/
        /*return DevWorldConfig.RULE_DOFIRETICK.get();
        *//*?} elif neoforge {*/
        return DevWorldConfig.RULE_DOFIRETICK.get();
        /*?} else {*/
        /*return Config.EXTERNAL_CONFIG.gameRulesConfig.ruleDoFireTick != null ?
            Config.EXTERNAL_CONFIG.gameRulesConfig.ruleDoFireTick : false;
        *//*?}*/
    }

    private boolean getRuleMobGriefing() {
        /*? if forge {*/
        /*return DevWorldConfig.RULE_MOBGRIEFING.get();
        *//*?} elif neoforge {*/
        return DevWorldConfig.RULE_MOBGRIEFING.get();
        /*?} else {*/
        /*return Config.EXTERNAL_CONFIG.gameRulesConfig.ruleMobGriefing != null ?
            Config.EXTERNAL_CONFIG.gameRulesConfig.ruleMobGriefing : false;
        *//*?}*/
    }

    private boolean getRuleDoMobSpawning() {
        /*? if forge {*/
        /*return DevWorldConfig.RULE_DOMOBSPAWNING.get();
        *//*?} elif neoforge {*/
        return DevWorldConfig.RULE_DOMOBSPAWNING.get();
        /*?} else {*/
        /*return Config.EXTERNAL_CONFIG.gameRulesConfig.ruleDoMobSpawning != null ?
            Config.EXTERNAL_CONFIG.gameRulesConfig.ruleDoMobSpawning : true;
        *//*?}*/
    }

    private boolean getRuleDisableRaids() {
        /*? if forge {*/
        /*return DevWorldConfig.RULE_DISABLE_RAIDS.get();
        *//*?} elif neoforge {*/
        return DevWorldConfig.RULE_DISABLE_RAIDS.get();
        /*?} else {*/
        /*return Config.EXTERNAL_CONFIG.gameRulesConfig.ruleDisableRaids;
        *//*?}*/
    }

    private boolean getRuleDoInsomnia() {
        /*? if forge {*/
        /*return DevWorldConfig.RULE_DOINSOMNIA.get();
        *//*?} elif neoforge {*/
        return DevWorldConfig.RULE_DOINSOMNIA.get();
        /*?} else {*/
        /*return Config.EXTERNAL_CONFIG.gameRulesConfig.ruleDoInsomnia != null ?
            Config.EXTERNAL_CONFIG.gameRulesConfig.ruleDoInsomnia : false;
        *//*?}*/
    }
}
