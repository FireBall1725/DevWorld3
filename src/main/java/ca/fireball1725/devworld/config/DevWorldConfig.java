package ca.fireball1725.devworld.config;

/*? if forge {*/
import net.minecraftforge.common.ForgeConfigSpec;
/*?} elif neoforge {*/
/*import net.neoforged.neoforge.common.ModConfigSpec;
/*?} elif fabric {*/
/*import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
/*?}*/

public class DevWorldConfig {
    /*? if forgeLike {*/
    /*? if forge {*/
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
    /*?} elif neoforge {*/
    /*public static ModConfigSpec.ConfigValue<String> FLATWORLD_GENERATOR_STRING;
    public static ModConfigSpec.ConfigValue<Boolean> ENABLE_BONUS_CHEST;
    public static ModConfigSpec.ConfigValue<Boolean> RULE_DAYLIGHT;
    public static ModConfigSpec.ConfigValue<Integer> DAYLIGHT_VALUE;
    public static ModConfigSpec.ConfigValue<Boolean> RULE_WEATHER_CYCLE;
    public static ModConfigSpec.ConfigValue<Boolean> RULE_DOFIRETICK;
    public static ModConfigSpec.ConfigValue<Boolean> RULE_MOBGRIEFING;
    public static ModConfigSpec.ConfigValue<Boolean> RULE_DOMOBSPAWNING;
    public static ModConfigSpec.ConfigValue<Boolean> RULE_DISABLE_RAIDS;
    public static ModConfigSpec.ConfigValue<Boolean> RULE_DOINSOMNIA;

    public static void registerClientConfig(ModConfigSpec.Builder CLIENT_BUILDER) {
    /*?}*/
        CLIENT_BUILDER.comment("World generation configuration").push("world_config");

        FLATWORLD_GENERATOR_STRING = CLIENT_BUILDER
                .comment("Flat world generator preset")
                .define("world_generation_preset", Config.EXTERNAL_CONFIG.worldConfig.worldGenerationPreset);

        ENABLE_BONUS_CHEST = CLIENT_BUILDER
                .comment("Enable the bonus chest")
                .define("bonus_chest", Config.EXTERNAL_CONFIG.worldConfig.bonusChest);

        CLIENT_BUILDER.pop();
    }

    /*? if forge {*/
    public static void registerGameRuleConfig(ForgeConfigSpec.Builder CLIENT_BUILDER) {
    /*?} elif neoforge {*/
    /*public static void registerGameRuleConfig(ModConfigSpec.Builder CLIENT_BUILDER) {
    /*?}*/
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
    /*?} elif fabric {*/
    /*// Fabric config - simple static values loaded from JSON
    private static FabricConfigData configData = new FabricConfigData();

    public static void initDefaults() {
        configData = new FabricConfigData();
    }

    public static void loadConfig(Path path) throws IOException {
        if (Files.exists(path)) {
            Gson gson = new Gson();
            String json = Files.readString(path);
            configData = gson.fromJson(json, FabricConfigData.class);
        } else {
            configData = new FabricConfigData();
        }
    }

    public static void saveConfig(Path path) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Files.writeString(path, gson.toJson(configData));
    }

    // Static wrapper class for Fabric config values
    public static class ConfigValue<T> {
        private final T value;
        public ConfigValue(T value) { this.value = value; }
        public T get() { return value; }
    }

    public static final ConfigValue<String> FLATWORLD_GENERATOR_STRING =
            new ConfigValue<>(Config.EXTERNAL_CONFIG.worldConfig.worldGenerationPreset);
    public static final ConfigValue<Boolean> ENABLE_BONUS_CHEST =
            new ConfigValue<>(Config.EXTERNAL_CONFIG.worldConfig.bonusChest);
    public static final ConfigValue<Boolean> RULE_DAYLIGHT =
            new ConfigValue<>(Config.EXTERNAL_CONFIG.gameRulesConfig.ruleDayLightCycle);
    public static final ConfigValue<Integer> DAYLIGHT_VALUE =
            new ConfigValue<>(Config.EXTERNAL_CONFIG.gameRulesConfig.daylightTime);
    public static final ConfigValue<Boolean> RULE_WEATHER_CYCLE =
            new ConfigValue<>(Config.EXTERNAL_CONFIG.gameRulesConfig.ruleWeatherCycle);
    public static final ConfigValue<Boolean> RULE_DOFIRETICK =
            new ConfigValue<>(Config.EXTERNAL_CONFIG.gameRulesConfig.ruleDoFireTick);
    public static final ConfigValue<Boolean> RULE_MOBGRIEFING =
            new ConfigValue<>(Config.EXTERNAL_CONFIG.gameRulesConfig.ruleMobGriefing);
    public static final ConfigValue<Boolean> RULE_DOMOBSPAWNING =
            new ConfigValue<>(Config.EXTERNAL_CONFIG.gameRulesConfig.ruleDoMobSpawning);
    public static final ConfigValue<Boolean> RULE_DISABLE_RAIDS =
            new ConfigValue<>(Config.EXTERNAL_CONFIG.gameRulesConfig.ruleDisableRaids);
    public static final ConfigValue<Boolean> RULE_DOINSOMNIA =
            new ConfigValue<>(Config.EXTERNAL_CONFIG.gameRulesConfig.ruleDoInsomnia);

    private static class FabricConfigData {
        public String flatworldGeneratorString = "minecraft:bedrock,3*minecraft:dirt,minecraft:grass_block;minecraft:plains";
        public boolean enableBonusChest = false;
        public boolean ruleDaylight = false;
        public int daylightValue = 6000;
        public boolean ruleWeatherCycle = false;
        public boolean ruleDoFireTick = false;
        public boolean ruleMobGriefing = false;
        public boolean ruleDoMobSpawning = true;
        public boolean ruleDisableRaids = true;
        public boolean ruleDoInsomnia = false;
    }
    /*?}*/
}
