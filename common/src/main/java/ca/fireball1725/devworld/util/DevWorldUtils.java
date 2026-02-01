package ca.fireball1725.devworld.util;

import ca.fireball1725.devworld.config.DevWorldConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
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
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for creating, loading, and managing DevWorld instances in Minecraft 1.21.1.
 * Handles world creation with custom game rule configuration.
 */
public class DevWorldUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final String worldName = "DevWorld";
    private final Minecraft minecraft = Minecraft.getInstance();
    private final DevWorldConfig config;

    /**
     * Creates a new DevWorldUtils instance.
     * @param config The configuration interface providing mod settings
     */
    public DevWorldUtils(DevWorldConfig config) {
        this.config = config;
    }

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
        // Using DEFAULT_FLAGS which contains vanilla features but excludes experimental ones
        WorldDataConfiguration worldDataConfig = new WorldDataConfiguration(
                net.minecraft.world.level.DataPackConfig.DEFAULT,
                net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS // Vanilla features only
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
                false, // generateStructures (flat worlds typically don't generate structures)
                config.getEnableBonusChest() // bonusChest
        );

        // Use the world creation flow to properly load registries and create the world
        // The signature is: createFreshLevel(String, LevelSettings, WorldOptions, Function<RegistryAccess, WorldDimensions>, Screen)
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
     * Note: The getDaylightValue() config cannot be applied during world creation.
     * To set a specific time value, you would need to use a command like /time set
     * after the world loads, or implement a world load event handler.
     */
    private GameRules createGameRules() {
        GameRules gameRules = new GameRules();

        // Daylight cycle control
        gameRules.getRule(GameRules.RULE_DAYLIGHT).set(config.getRuleDaylight(), null);

        // Weather and environmental rules
        gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(config.getRuleWeatherCycle(), null);
        gameRules.getRule(GameRules.RULE_DOFIRETICK).set(config.getRuleDoFireTick(), null);

        // Mob-related rules
        gameRules.getRule(GameRules.RULE_MOBGRIEFING).set(config.getRuleMobGriefing(), null);
        gameRules.getRule(GameRules.RULE_DOMOBSPAWNING).set(config.getRuleDoMobSpawning(), null);
        gameRules.getRule(GameRules.RULE_DISABLE_RAIDS).set(config.getRuleDisableRaids(), null);
        gameRules.getRule(GameRules.RULE_DOINSOMNIA).set(config.getRuleDoInsomnia(), null);

        return gameRules;
    }

    /**
     * Creates flat world dimensions from custom preset string or falls back to default.
     * Parses preset format: "layers;biome" where layers are "[count*]block_id,block_id,..."
     * Example: "minecraft:bedrock,3*minecraft:stone,116*minecraft:sandstone;minecraft:desert"
     *
     * @param registryAccess The registry access for looking up blocks and biomes
     * @return WorldDimensions configured for flat world generation
     */
    private WorldDimensions createFlatWorldDimensions(RegistryAccess registryAccess) {
        String presetString = config.getFlatworldGeneratorString();

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

    /**
     * Creates default flat world dimensions using the built-in flat preset.
     */
    private WorldDimensions createDefaultFlatWorld(RegistryAccess registryAccess) {
        return registryAccess.lookupOrThrow(Registries.WORLD_PRESET)
                .getOrThrow(WorldPresets.FLAT)
                .value()
                .createWorldDimensions();
    }

    /**
     * Parses a flat world preset string into FlatLevelGeneratorSettings.
     * Format: "layer1,layer2,...;biome"
     * Layer format: "[count*]block_id" where count defaults to 1 if omitted
     *
     * @param presetString The preset string to parse
     * @param registryAccess Registry access for blocks and biomes
     * @return Configured FlatLevelGeneratorSettings
     * @throws IllegalArgumentException if the preset string is malformed
     */
    private FlatLevelGeneratorSettings parsePresetString(String presetString, RegistryAccess registryAccess) {
        // Split on semicolon to separate layers from biome
        String[] parts = presetString.split(";", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Preset must contain layers and biome separated by ';'");
        }

        String layersString = parts[0].trim();
        String biomeString = parts[1].trim();

        // Parse layers (bottom to top)
        List<FlatLayerInfo> layers = parseLayerString(layersString, registryAccess);

        // Parse biome
        Holder<Biome> biome = parseBiome(biomeString, registryAccess);

        // Get registries needed for FlatLevelGeneratorSettings
        HolderGetter<Biome> biomeRegistry = registryAccess.lookupOrThrow(Registries.BIOME);
        HolderGetter<StructureSet> structureSetRegistry = registryAccess.lookupOrThrow(Registries.STRUCTURE_SET);
        HolderGetter<PlacedFeature> placedFeatureRegistry = registryAccess.lookupOrThrow(Registries.PLACED_FEATURE);

        // Create default settings first (provides structure overrides and placed features)
        FlatLevelGeneratorSettings defaultSettings = FlatLevelGeneratorSettings.getDefault(
                biomeRegistry,
                structureSetRegistry,
                placedFeatureRegistry
        );

        // Use withBiomeAndLayers to create custom settings with our layers and biome
        // This method signature: withBiomeAndLayers(List<FlatLayerInfo> layers, Optional<HolderSet<StructureSet>> structures, Holder<Biome> biome)
        return defaultSettings.withBiomeAndLayers(
                layers,
                defaultSettings.structureOverrides(), // Keep default structure overrides
                biome
        );
    }

    /**
     * Parses the layer portion of the preset string.
     * Format: "layer1,layer2,..." where each layer is "[count*]block_id"
     *
     * @param layersString The layers portion of the preset
     * @param registryAccess Registry access for blocks
     * @return List of FlatLayerInfo in bottom-to-top order
     */
    private List<FlatLayerInfo> parseLayerString(String layersString, RegistryAccess registryAccess) {
        if (layersString.isEmpty()) {
            throw new IllegalArgumentException("Layers string cannot be empty");
        }

        HolderGetter<Block> blockRegistry = registryAccess.lookupOrThrow(Registries.BLOCK);
        List<FlatLayerInfo> layers = new ArrayList<>();

        String[] layerParts = layersString.split(",");
        for (String layerPart : layerParts) {
            layerPart = layerPart.trim();
            if (layerPart.isEmpty()) {
                continue;
            }

            int count = 1;
            String blockId;

            // Check if layer specifies count (e.g., "3*minecraft:stone")
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

            // Parse block resource location
            ResourceLocation blockResourceLocation;
            try {
                blockResourceLocation = ResourceLocation.parse(blockId);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid block ID: " + blockId, e);
            }

            // Look up block in registry
            ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, blockResourceLocation);
            Optional<Holder.Reference<Block>> blockHolder = blockRegistry.get(blockKey);

            if (blockHolder.isEmpty()) {
                throw new IllegalArgumentException("Unknown block: " + blockId);
            }

            // Get the block and its default state
            Block block = blockHolder.get().value();
            BlockState blockState = block.defaultBlockState();

            // Create layer info and add to list
            // FlatLayerInfo constructor takes (int height, Block block)
            layers.add(new FlatLayerInfo(count, block));
        }

        if (layers.isEmpty()) {
            throw new IllegalArgumentException("No valid layers found in preset");
        }

        return layers;
    }

    /**
     * Parses a biome resource location into a Biome holder.
     *
     * @param biomeString The biome resource location string
     * @param registryAccess Registry access for biomes
     * @return Holder for the biome
     */
    private Holder<Biome> parseBiome(String biomeString, RegistryAccess registryAccess) {
        if (biomeString.isEmpty()) {
            throw new IllegalArgumentException("Biome string cannot be empty");
        }

        // Parse biome resource location
        ResourceLocation biomeResourceLocation;
        try {
            biomeResourceLocation = ResourceLocation.parse(biomeString);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid biome ID: " + biomeString, e);
        }

        // Look up biome in registry
        HolderGetter<Biome> biomeRegistry = registryAccess.lookupOrThrow(Registries.BIOME);
        ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, biomeResourceLocation);
        Optional<Holder.Reference<Biome>> biomeHolder = biomeRegistry.get(biomeKey);

        if (biomeHolder.isEmpty()) {
            LOGGER.warn("Unknown biome '{}', falling back to plains", biomeString);
            // Fall back to plains if biome not found
            return biomeRegistry.getOrThrow(Biomes.PLAINS);
        }

        return biomeHolder.get();
    }

    /**
     * Creates WorldDimensions from FlatLevelGeneratorSettings.
     * This constructs the overworld dimension with flat terrain generation.
     *
     * @param settings The flat level generator settings
     * @param registryAccess Registry access for dimension construction
     * @return WorldDimensions configured with the flat settings
     */
    private WorldDimensions createWorldDimensionsFromFlatSettings(
            FlatLevelGeneratorSettings settings,
            RegistryAccess registryAccess) {

        // Get the flat world preset to use as a base (has all three dimensions)
        net.minecraft.world.level.levelgen.presets.WorldPreset flatPreset =
                registryAccess.lookupOrThrow(Registries.WORLD_PRESET)
                        .getOrThrow(net.minecraft.world.level.levelgen.presets.WorldPresets.FLAT)
                        .value();

        // Create base dimensions from flat preset
        WorldDimensions baseDimensions = flatPreset.createWorldDimensions();

        // Replace the overworld dimension with our custom flat settings
        net.minecraft.world.level.chunk.ChunkGenerator customOverworldGenerator =
                new net.minecraft.world.level.levelgen.FlatLevelSource(settings);

        // Get the overworld dimension type
        HolderGetter<net.minecraft.world.level.dimension.DimensionType> dimensionTypes =
                registryAccess.lookupOrThrow(Registries.DIMENSION_TYPE);
        Holder<net.minecraft.world.level.dimension.DimensionType> overworldType =
                dimensionTypes.getOrThrow(net.minecraft.world.level.dimension.BuiltinDimensionTypes.OVERWORLD);

        // Create custom overworld stem
        LevelStem customOverworldStem = new LevelStem(overworldType, customOverworldGenerator);

        // Get the base dimensions registry and create a new one with our custom overworld
        net.minecraft.core.MappedRegistry<LevelStem> levelStemRegistry =
                new net.minecraft.core.MappedRegistry<>(Registries.LEVEL_STEM, com.mojang.serialization.Lifecycle.stable());

        // Register custom overworld
        levelStemRegistry.register(
                ResourceKey.create(Registries.LEVEL_STEM, Level.OVERWORLD.location()),
                customOverworldStem,
                net.minecraft.core.RegistrationInfo.BUILT_IN
        );

        // Copy nether and end from the base flat preset dimensions
        baseDimensions.dimensions().entrySet().stream()
                .filter(entry -> !entry.getKey().location().equals(Level.OVERWORLD.location()))
                .forEach(entry -> levelStemRegistry.register(
                        entry.getKey(),
                        entry.getValue(),
                        net.minecraft.core.RegistrationInfo.BUILT_IN
                ));

        // Create WorldDimensions with all three standard dimensions
        return new WorldDimensions(levelStemRegistry);
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
        // In 1.21.1, openWorld takes a String parameter and a Runnable
        this.minecraft.createWorldOpenFlows().openWorld(this.worldName, () -> minecraft.setScreen(null));
    }

    /**
     * Delete the dev world
     */
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
