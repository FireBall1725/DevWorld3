package ca.fireball1725.devworld.neoforge.config;

import ca.fireball1725.devworld.config.DevWorldConfig;
import ca.fireball1725.devworld.config.ExternalConfigLoader;
import ca.fireball1725.devworld.dataclass.ExternalConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class DevWorldConfigImpl implements DevWorldConfig {
    private static final ModConfigSpec SPEC;
    private static final Config CONFIG;
    private static final DevWorldConfigImpl INSTANCE;

    static {
        Pair<Config, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Config::new);
        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
        INSTANCE = new DevWorldConfigImpl();
    }

    public static ModConfigSpec getSpec() {
        return SPEC;
    }

    public static DevWorldConfigImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean getEnableBonusChest() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.worldConfig.bonusChest != null ? external.worldConfig.bonusChest : false;
        }
        return CONFIG.enableBonusChest.get();
    }

    @Override
    public String getFlatworldGeneratorString() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.worldConfig.worldGenerationPreset;
        }
        return CONFIG.flatworldGeneratorString.get();
    }

    @Override
    public boolean getRuleDaylight() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.gameRulesConfig.ruleDayLightCycle != null ? external.gameRulesConfig.ruleDayLightCycle : false;
        }
        return CONFIG.ruleDaylight.get();
    }

    @Override
    public int getDaylightValue() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.gameRulesConfig.daylightTime != null ? external.gameRulesConfig.daylightTime : 6000;
        }
        return CONFIG.daylightValue.get();
    }

    @Override
    public boolean getRuleWeatherCycle() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.gameRulesConfig.ruleWeatherCycle != null ? external.gameRulesConfig.ruleWeatherCycle : false;
        }
        return CONFIG.ruleWeatherCycle.get();
    }

    @Override
    public boolean getRuleDoFireTick() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.gameRulesConfig.ruleDoFireTick != null ? external.gameRulesConfig.ruleDoFireTick : false;
        }
        return CONFIG.ruleDoFireTick.get();
    }

    @Override
    public boolean getRuleMobGriefing() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.gameRulesConfig.ruleMobGriefing != null ? external.gameRulesConfig.ruleMobGriefing : false;
        }
        return CONFIG.ruleMobGriefing.get();
    }

    @Override
    public boolean getRuleDoMobSpawning() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.gameRulesConfig.ruleDoMobSpawning != null ? external.gameRulesConfig.ruleDoMobSpawning : true;
        }
        return CONFIG.ruleDoMobSpawning.get();
    }

    @Override
    public boolean getRuleDisableRaids() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.gameRulesConfig.ruleDisableRaids;
        }
        return CONFIG.ruleDisableRaids.get();
    }

    @Override
    public boolean getRuleDoInsomnia() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.gameRulesConfig.ruleDoInsomnia != null ? external.gameRulesConfig.ruleDoInsomnia : false;
        }
        return CONFIG.ruleDoInsomnia.get();
    }

    private static class Config {
        final ModConfigSpec.BooleanValue enableBonusChest;
        final ModConfigSpec.ConfigValue<String> flatworldGeneratorString;
        final ModConfigSpec.BooleanValue ruleDaylight;
        final ModConfigSpec.IntValue daylightValue;
        final ModConfigSpec.BooleanValue ruleWeatherCycle;
        final ModConfigSpec.BooleanValue ruleDoFireTick;
        final ModConfigSpec.BooleanValue ruleMobGriefing;
        final ModConfigSpec.BooleanValue ruleDoMobSpawning;
        final ModConfigSpec.BooleanValue ruleDisableRaids;
        final ModConfigSpec.BooleanValue ruleDoInsomnia;

        Config(ModConfigSpec.Builder builder) {
            builder.comment("World generation configuration").push("world_config");

            enableBonusChest = builder
                .comment("Enable bonus chest")
                .define("enableBonusChest", false);

            flatworldGeneratorString = builder
                .comment("Flat world generator preset string")
                .define("flatworldGeneratorString",
                    "minecraft:bedrock,3*minecraft:dirt,minecraft:grass_block;minecraft:plains");

            builder.pop();

            builder.comment("Game rule configuration").push("game_rules");

            ruleDaylight = builder
                .comment("Should the daylight cycle be enabled")
                .define("ruleDaylight", false);

            daylightValue = builder
                .comment("Default time to lock day/night to (6000 = noon)")
                .defineInRange("daylightValue", 6000, 0, 24000);

            ruleWeatherCycle = builder
                .comment("Should the weather cycle be enabled")
                .define("ruleWeatherCycle", false);

            ruleDoFireTick = builder
                .comment("Should fire tick be enabled")
                .define("ruleDoFireTick", false);

            ruleMobGriefing = builder
                .comment("Should mob griefing be enabled")
                .define("ruleMobGriefing", false);

            ruleDoMobSpawning = builder
                .comment("Should mob spawning be enabled")
                .define("ruleDoMobSpawning", true);

            ruleDisableRaids = builder
                .comment("Should raids be disabled")
                .define("ruleDisableRaids", true);

            ruleDoInsomnia = builder
                .comment("Should insomnia be enabled")
                .define("ruleDoInsomnia", false);

            builder.pop();
        }
    }
}
