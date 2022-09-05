package ca.fireball1725.devworld.config;

import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class DevWorldConfig {
    public static ForgeConfigSpec.ConfigValue<String> FLATWORLD_GENERATOR_STRING;
    public static ForgeConfigSpec.ConfigValue<Boolean> ENABLE_BONUS_CHEST;

    public static ForgeConfigSpec.ConfigValue<Boolean> GAMERULE_DAYLIGHT_CYCLE;
    public static ForgeConfigSpec.ConfigValue<Integer> GAMERULE_TIME_VALUE;
    public static ForgeConfigSpec.ConfigValue<Boolean> GAMERULE_WEATHER_CYCLE;
    public static void registerClientConfig(ForgeConfigSpec.Builder CLIENT_BUILDER) {
        CLIENT_BUILDER.comment("World generation configuration").push("world_config");

        FLATWORLD_GENERATOR_STRING = CLIENT_BUILDER
                .comment("Flat world generator preset")
                .define("world_generation_preset", Config.EXTERNAL_CONFIG.worldConfig.worldGenerationPreset);

        ENABLE_BONUS_CHEST = CLIENT_BUILDER
                .comment("Enable the bonus chest")
                .define("bonus_chest", Config.EXTERNAL_CONFIG.worldConfig.bonusChest);

        CLIENT_BUILDER.pop();
    }

    public static void registerGameRuleConfig(ForgeConfigSpec.Builder CLIENT_BUILDER) {
        CLIENT_BUILDER.comment("Gamerule configuration").push("game_rules");

        GAMERULE_DAYLIGHT_CYCLE = CLIENT_BUILDER
                .comment("Should the daylight cycle be enabled")
                .define("daylight_cycle", Config.EXTERNAL_CONFIG.gameRulesConfig.daylightCycle);

        GAMERULE_TIME_VALUE = CLIENT_BUILDER
                .comment("Default time to lock day / night to")
                .define("daylight_time", Config.EXTERNAL_CONFIG.gameRulesConfig.daylightTime);

        GAMERULE_WEATHER_CYCLE = CLIENT_BUILDER
                .comment("Should the weather cycle be enabled")
                .define("weather_cycle", Config.EXTERNAL_CONFIG.gameRulesConfig.weatherCycle);

        CLIENT_BUILDER.pop();
    }
}
