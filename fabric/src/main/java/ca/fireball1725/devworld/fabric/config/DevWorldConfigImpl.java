package ca.fireball1725.devworld.fabric.config;

import ca.fireball1725.devworld.DevWorld;
import ca.fireball1725.devworld.config.DevWorldConfig;
import ca.fireball1725.devworld.config.ExternalConfigLoader;
import ca.fireball1725.devworld.dataclass.ExternalConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DevWorldConfigImpl implements DevWorldConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("devworld.json");

    private static ConfigData data;
    private static final DevWorldConfigImpl INSTANCE = new DevWorldConfigImpl();

    public static void load() {
        // External config is loaded automatically by ExternalConfigLoader
        // We only need to load the local file config as fallback
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                data = GSON.fromJson(json, ConfigData.class);
                if (data == null) {
                    data = new ConfigData();
                }
            } catch (IOException e) {
                DevWorld.LOGGER.error("Failed to load config", e);
                data = new ConfigData();
            }
        } else {
            data = new ConfigData();
            save();
        }
    }

    private static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(data));
        } catch (IOException e) {
            DevWorld.LOGGER.error("Failed to save config", e);
        }
    }

    public static DevWorldConfigImpl getInstance() {
        if (data == null) {
            load();
        }
        return INSTANCE;
    }

    @Override
    public boolean getEnableBonusChest() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.worldConfig.bonusChest != null ? external.worldConfig.bonusChest : false;
        }
        return data.enableBonusChest;
    }

    @Override
    public String getFlatworldGeneratorString() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.worldConfig.worldGenerationPreset;
        }
        return data.flatworldGeneratorString;
    }

    @Override
    public boolean getRuleDaylight() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.gameRulesConfig.ruleDayLightCycle != null ? external.gameRulesConfig.ruleDayLightCycle : false;
        }
        return data.ruleDaylight;
    }

    @Override
    public int getDaylightValue() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.gameRulesConfig.daylightTime != null ? external.gameRulesConfig.daylightTime : 6000;
        }
        return data.daylightValue;
    }

    @Override
    public boolean getRuleWeatherCycle() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.gameRulesConfig.ruleWeatherCycle != null ? external.gameRulesConfig.ruleWeatherCycle : false;
        }
        return data.ruleWeatherCycle;
    }

    @Override
    public boolean getRuleDoFireTick() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.gameRulesConfig.ruleDoFireTick != null ? external.gameRulesConfig.ruleDoFireTick : false;
        }
        return data.ruleDoFireTick;
    }

    @Override
    public boolean getRuleMobGriefing() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.gameRulesConfig.ruleMobGriefing != null ? external.gameRulesConfig.ruleMobGriefing : false;
        }
        return data.ruleMobGriefing;
    }

    @Override
    public boolean getRuleDoMobSpawning() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.gameRulesConfig.ruleDoMobSpawning != null ? external.gameRulesConfig.ruleDoMobSpawning : true;
        }
        return data.ruleDoMobSpawning;
    }

    @Override
    public boolean getRuleDisableRaids() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.gameRulesConfig.ruleDisableRaids;
        }
        return data.ruleDisableRaids;
    }

    @Override
    public boolean getRuleDoInsomnia() {
        ExternalConfig external = ExternalConfigLoader.getExternalConfig();
        if (external != null) {
            return external.gameRulesConfig.ruleDoInsomnia != null ? external.gameRulesConfig.ruleDoInsomnia : false;
        }
        return data.ruleDoInsomnia;
    }

    private static class ConfigData {
        boolean enableBonusChest = false;
        String flatworldGeneratorString =
            "minecraft:bedrock,3*minecraft:dirt,minecraft:grass_block;minecraft:plains";
        boolean ruleDaylight = false;
        int daylightValue = 6000;
        boolean ruleWeatherCycle = false;
        boolean ruleDoFireTick = false;
        boolean ruleMobGriefing = false;
        boolean ruleDoMobSpawning = true;
        boolean ruleDisableRaids = true;
        boolean ruleDoInsomnia = false;
    }
}
