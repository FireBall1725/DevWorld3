package ca.fireball1725.devworld.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class DevWorldConfig {
    public static ForgeConfigSpec.ConfigValue<String> FLATWORLD_GENERATOR_STRING;
    public static ForgeConfigSpec.ConfigValue<Boolean> ENABLE_BONUS_CHEST;

    public static ForgeConfigSpec.ConfigValue<Boolean> RULE_DAYLIGHT;
    public static ForgeConfigSpec.ConfigValue<Integer> DAYLIGHT_VALUE;
    public static ForgeConfigSpec.ConfigValue<Boolean> RULE_WEATHER_CYCLE;
    public static ForgeConfigSpec.ConfigValue<Boolean> RULE_DOFIRETICK;
    public static ForgeConfigSpec.ConfigValue<Boolean> RULE_MOBGRIEFING;
    public static ForgeConfigSpec.ConfigValue<Boolean> RULE_DOMOBSPAWNING;
    public static ForgeConfigSpec.ConfigValue<Boolean> RULE_DISABLE_RAIDS;
    public static ForgeConfigSpec.ConfigValue<Boolean> RULE_DOINSOMNIA;
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

        RULE_DAYLIGHT = CLIENT_BUILDER
                .comment("Should the daylight cycle be enabled")
                .define("rule_daylight", Config.EXTERNAL_CONFIG.gameRulesConfig.ruleDayLightCycle);

        DAYLIGHT_VALUE = CLIENT_BUILDER
                .comment("Default time to lock day / night to")
                .define("daylight_time", Config.EXTERNAL_CONFIG.gameRulesConfig.daylightTime);

        RULE_WEATHER_CYCLE = CLIENT_BUILDER
                .comment("Should the weather cycle be enabled")
                .define("rule_weather_cycle", Config.EXTERNAL_CONFIG.gameRulesConfig.ruleWeatherCycle);

        RULE_DOFIRETICK = CLIENT_BUILDER
                .comment("Should the fire tick be enabled")
                .define("rule_dofiretick", Config.EXTERNAL_CONFIG.gameRulesConfig.ruleDoFireTick);

        RULE_MOBGRIEFING = CLIENT_BUILDER
                .comment("Should mob griefing be enabled")
                .define("rule_mobgriefing", Config.EXTERNAL_CONFIG.gameRulesConfig.ruleMobGriefing);

        RULE_DOMOBSPAWNING = CLIENT_BUILDER
                .comment("Should mob spawning be enabled")
                .define("rule_domobspawning", Config.EXTERNAL_CONFIG.gameRulesConfig.ruleDoMobSpawning);

        RULE_DISABLE_RAIDS = CLIENT_BUILDER
                .comment("Should the raids be disabled")
                .define("rule_disable_raids", Config.EXTERNAL_CONFIG.gameRulesConfig.ruleDisableRaids);

        RULE_DOINSOMNIA = CLIENT_BUILDER
                .comment("Should the insomnia be enabled")
                .define("rule_doinsomnia", Config.EXTERNAL_CONFIG.gameRulesConfig.ruleDoInsomnia);

        CLIENT_BUILDER.pop();
    }
}
