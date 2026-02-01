package ca.fireball1725.devworld.util;

import ca.fireball1725.devworld.config.DevWorldConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.PresetFlatWorldScreen;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;

import java.io.IOException;

public class DevWorldUtils {
    private final String worldName = "DevWorld";
    private final Minecraft minecraft = Minecraft.getInstance();
    private final Component PREPARING_WORLD_DATA = Component.translatable("createWorld.preparing");
    private final Logger LOGGER = LogUtils.getLogger();

    String resultFolder;


    /**
     * Create a new dev world using the simplified approach.
     * This uses Minecraft's built-in world creation flow which handles all registry setup internally.
     * @throws Exception if there is an issue with the save
     */
    public void createDevWorld() throws Exception {
        this.resultFolder = this.worldName.trim();

        // Show loading screen
        queueLoadScreen();

        // Parse the flat world preset string to get settings
        String flatPreset = DevWorldConfig.FLATWORLD_GENERATOR_STRING.get();

        // Create the level settings with game rules
        LevelSettings levelSettings = createLevelSettings();

        // Use Minecraft's built-in fresh level creation which handles registry setup
        // This is the same approach used by vanilla CreateWorldScreen
        this.minecraft.createWorldOpenFlows().createFreshLevel(
                this.resultFolder,
                levelSettings,
                new WorldOptions(
                        0L,      // seed
                        false,   // generateStructures
                        DevWorldConfig.ENABLE_BONUS_CHEST.get()  // generateBonusChest
                ),
                // WorldPresets function to create dimensions - we use a custom function for flat world
                (registryAccess) -> {
                    try {
                        // Get the required registry lookups
                        HolderLookup.RegistryLookup<net.minecraft.world.level.block.Block> blockLookup =
                                registryAccess.lookupOrThrow(Registries.BLOCK);
                        HolderLookup.RegistryLookup<net.minecraft.world.level.biome.Biome> biomeLookup =
                                registryAccess.lookupOrThrow(Registries.BIOME);
                        HolderLookup.RegistryLookup<net.minecraft.world.level.levelgen.structure.StructureSet> structureSetLookup =
                                registryAccess.lookupOrThrow(Registries.STRUCTURE_SET);
                        HolderLookup.RegistryLookup<net.minecraft.world.level.levelgen.placement.PlacedFeature> placedFeatureLookup =
                                registryAccess.lookupOrThrow(Registries.PLACED_FEATURE);

                        // Parse the flat world preset
                        FlatLevelGeneratorSettings flatSettings = PresetFlatWorldScreen.fromString(
                                blockLookup,
                                biomeLookup,
                                structureSetLookup,
                                placedFeatureLookup,
                                flatPreset,
                                FlatLevelGeneratorSettings.getDefault(biomeLookup, structureSetLookup, placedFeatureLookup)
                        );

                        // Create flat world chunk generator
                        ChunkGenerator flatChunkGenerator = new FlatLevelSource(flatSettings);

                        // Start with normal dimensions and replace overworld with flat generator
                        WorldDimensions worldDimensions = WorldPresets.createNormalWorldDimensions(registryAccess);
                        return worldDimensions.replaceOverworldGenerator(registryAccess, flatChunkGenerator);

                    } catch (Exception e) {
                        LOGGER.error("Failed to create flat world generator, falling back to default", e);
                        // Fallback to normal world if flat world setup fails
                        return WorldPresets.createNormalWorldDimensions(registryAccess);
                    }
                }
        );
    }

    /**
     * Show loading screen
     */
    private void queueLoadScreen() {
        this.minecraft.forceSetScreen(new GenericDirtMessageScreen(this.PREPARING_WORLD_DATA));
    }

    /**
     * Create the level settings
     * @return the level settings
     */
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

        return new LevelSettings(
                s,
                GameType.CREATIVE,
                false,
                Difficulty.NORMAL,
                true,
                gameRules,
                WorldDataConfiguration.DEFAULT
        );
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
