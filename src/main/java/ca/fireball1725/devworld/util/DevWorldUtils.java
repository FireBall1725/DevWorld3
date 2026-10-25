package ca.fireball1725.devworld.util;

import ca.fireball1725.devworld.DevWorldCommon;
import ca.fireball1725.devworld.config.DevWorldConfig;
import ca.fireball1725.devworld.dataclass.WorldConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.PresetFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.RegistryAccess;
//? if <1.21
//import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
//? if >=1.20 {
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.WorldDataConfiguration;
//?}
//? if <1.20 {
/*import net.minecraft.core.Registry;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
*///?}

//? if <1.21
//import net.minecraft.client.gui.screens.GenericDirtMessageScreen;

import java.io.IOException;
//? if >=1.20
import java.util.List;
import java.util.Optional;

/** Creates, loads, deletes and checks for the DevWorld save. */
public class DevWorldUtils {
    private static String worldName() {
        return DevWorldConfig.world().worldName;
    }

    private Minecraft minecraft() {
        return Minecraft.getInstance();
    }

    /** Creates a new dev world from the config and opens it. */
    public void createDevWorld() {
        Minecraft mc = minecraft();
        Screen previous = mc.screen;
        LevelSettings levelSettings = createLevelSettings();
        WorldConfig world = DevWorldConfig.world();
        String preset = world.worldGenerationPreset;

        // 1.21 shows its own loading screen
        //? if <1.21
        //mc.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("createWorld.preparing")));

        //? if >=1.20 {
        WorldOptions options = new WorldOptions(world.seed, world.generateStructures, world.bonusChest);
        //? if >=1.21 {
        mc.createWorldOpenFlows().createFreshLevel(worldName(), levelSettings, options,
                access -> flatDimensions(access, preset), previous);
        //?}
        //? if <1.21 {
        /*mc.createWorldOpenFlows().createFreshLevel(worldName(), levelSettings, options,
                access -> flatDimensions(access, preset));
        *///?}
        //?}
        //? if <1.20 {
        /*RegistryAccess.Frozen access = RegistryAccess.builtinCopy().freeze();
        WorldGenSettings flat = flatWorldGenSettings(access, preset, world);
        mc.createWorldOpenFlows().createFreshLevel(worldName(), levelSettings, access, flat);
        *///?}
    }

    //? if >=1.20 {
    /** The normal dimensions with the overworld swapped for a flat world built from the preset string. */
    private static WorldDimensions flatDimensions(RegistryAccess access, String preset) {
        HolderGetter<Block> blocks = access.lookupOrThrow(Registries.BLOCK);
        HolderGetter<Biome> biomes = access.lookupOrThrow(Registries.BIOME);
        HolderGetter<StructureSet> structureSets = access.lookupOrThrow(Registries.STRUCTURE_SET);
        HolderGetter<PlacedFeature> features = access.lookupOrThrow(Registries.PLACED_FEATURE);

        // No structure overrides or lakes; the biome and layers are replaced by the preset string
        FlatLevelGeneratorSettings blank = new FlatLevelGeneratorSettings(
                Optional.empty(), biomes.getOrThrow(Biomes.PLAINS), List.of());
        FlatLevelGeneratorSettings flat = PresetFlatWorldScreen.fromString(
                blocks, biomes, structureSets, features, preset, blank);

        return WorldPresets.createNormalWorldDimensions(access)
                .replaceOverworldGenerator(access, new FlatLevelSource(flat));
    }
    //?}
    //? if <1.20 {
    /*private static WorldGenSettings flatWorldGenSettings(RegistryAccess access, String preset, WorldConfig world) {
        Registry<Biome> biomes = access.registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<StructureSet> structureSets = access.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);

        WorldGenSettings normal = WorldPresets.createNormalWorldFromPreset(access, world.seed, world.generateStructures,
                world.bonusChest);
        FlatLevelGeneratorSettings flat = PresetFlatWorldScreen.fromString(
                biomes, structureSets, preset, new FlatLevelGeneratorSettings(Optional.empty(), biomes));

        return WorldGenSettings.replaceOverworldGenerator(access, normal, new FlatLevelSource(structureSets, flat));
    }
    *///?}

    private LevelSettings createLevelSettings() {
        GameRules gameRules = new GameRules();
        GameRuleApplier.apply(gameRules, DevWorldConfig.gameRules());

        //? if >=1.20
        WorldDataConfiguration dataConfiguration = WorldDataConfiguration.DEFAULT;
        //? if <1.20
        //DataPackConfig dataConfiguration = DataPackConfig.DEFAULT;

        WorldConfig world = DevWorldConfig.world();
        return new LevelSettings(worldName(), GameType.byName(world.gameMode, GameType.CREATIVE), false,
                Difficulty.byName(world.difficulty), world.allowCommands, gameRules, dataConfiguration);
    }

    /** Whether there is a dev world save. */
    public boolean saveExists() {
        return minecraft().getLevelSource().levelExists(worldName());
    }

    /** Opens the existing dev world. */
    public void loadDevWorld() {
        Minecraft mc = minecraft();
        //? if >=1.21 {
        Screen previous = mc.screen;
        mc.createWorldOpenFlows().openWorld(worldName(), () -> mc.setScreen(previous));
        //?}
        //? if <1.21
        //mc.createWorldOpenFlows().loadLevel(mc.screen, worldName());
    }

    /** Deletes the dev world save. */
    public void deleteDevWorld() {
        Minecraft mc = minecraft();

        try (LevelStorageSource.LevelStorageAccess access = mc.getLevelSource().createAccess(worldName())) {
            access.deleteLevel();
        } catch (IOException ex) {
            SystemToast.onWorldDeleteFailure(mc, worldName());
            DevWorldCommon.LOGGER.error("Failed to delete world {}", worldName(), ex);
        }
    }
}
