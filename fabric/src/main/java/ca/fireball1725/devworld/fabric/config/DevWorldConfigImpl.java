package ca.fireball1725.devworld.fabric.config;

import ca.fireball1725.devworld.DevWorld;
import ca.fireball1725.devworld.config.DevWorldConfig;
import ca.fireball1725.devworld.dataclass.ExternalConfig;
import ca.fireball1725.devworld.dataclass.GameRulesConfig;
import ca.fireball1725.devworld.dataclass.WorldConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class DevWorldConfigImpl implements DevWorldConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("devworld.json");

    private static ConfigData data;
    private static ExternalConfig externalConfig;
    private static final DevWorldConfigImpl INSTANCE = new DevWorldConfigImpl();

    public static void load() {
        // First, check for DEVWORLD_CONFIG environment variable
        String devWorldConfigUrl = System.getenv("DEVWORLD_CONFIG");

        if (devWorldConfigUrl != null) {
            DevWorld.LOGGER.info("Found DEVWORLD_CONFIG environment variable, fetching from: {}", devWorldConfigUrl);
            try {
                String jsonConfig = readStringFromURL(devWorldConfigUrl);
                externalConfig = GSON.fromJson(jsonConfig, ExternalConfig.class);
                externalConfig = setDefaultsOnNull(externalConfig);
                DevWorld.LOGGER.info("Successfully loaded external config from URL");
                data = null; // Don't use local file config
                return;
            } catch (IOException e) {
                DevWorld.LOGGER.error("Failed to fetch external config from " + devWorldConfigUrl, e);
                DevWorld.LOGGER.info("Falling back to local config file");
            }
        }

        // Fall back to local file config
        externalConfig = null;
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

    private static String readStringFromURL(String requestURL) throws IOException {
        try (Scanner scanner = new Scanner(new URL(requestURL).openStream(),
                StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    private static ExternalConfig setDefaultsOnNull(ExternalConfig config) {
        if (config == null)
            config = new ExternalConfig();

        if (config.gameRulesConfig == null)
            config.gameRulesConfig = new GameRulesConfig();

        if (config.worldConfig == null)
            config.worldConfig = new WorldConfig();

        return config;
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
        if (externalConfig != null) {
            return externalConfig.worldConfig.bonusChest != null ? externalConfig.worldConfig.bonusChest : false;
        }
        return data.enableBonusChest;
    }

    @Override
    public String getFlatworldGeneratorString() {
        if (externalConfig != null) {
            return externalConfig.worldConfig.worldGenerationPreset;
        }
        return data.flatworldGeneratorString;
    }

    @Override
    public boolean getRuleDaylight() {
        if (externalConfig != null) {
            return externalConfig.gameRulesConfig.ruleDayLightCycle != null ? externalConfig.gameRulesConfig.ruleDayLightCycle : false;
        }
        return data.ruleDaylight;
    }

    @Override
    public int getDaylightValue() {
        if (externalConfig != null) {
            return externalConfig.gameRulesConfig.daylightTime != null ? externalConfig.gameRulesConfig.daylightTime : 6000;
        }
        return data.daylightValue;
    }

    @Override
    public boolean getRuleWeatherCycle() {
        if (externalConfig != null) {
            return externalConfig.gameRulesConfig.ruleWeatherCycle != null ? externalConfig.gameRulesConfig.ruleWeatherCycle : false;
        }
        return data.ruleWeatherCycle;
    }

    @Override
    public boolean getRuleDoFireTick() {
        if (externalConfig != null) {
            return externalConfig.gameRulesConfig.ruleDoFireTick != null ? externalConfig.gameRulesConfig.ruleDoFireTick : false;
        }
        return data.ruleDoFireTick;
    }

    @Override
    public boolean getRuleMobGriefing() {
        if (externalConfig != null) {
            return externalConfig.gameRulesConfig.ruleMobGriefing != null ? externalConfig.gameRulesConfig.ruleMobGriefing : false;
        }
        return data.ruleMobGriefing;
    }

    @Override
    public boolean getRuleDoMobSpawning() {
        if (externalConfig != null) {
            return externalConfig.gameRulesConfig.ruleDoMobSpawning != null ? externalConfig.gameRulesConfig.ruleDoMobSpawning : true;
        }
        return data.ruleDoMobSpawning;
    }

    @Override
    public boolean getRuleDisableRaids() {
        if (externalConfig != null) {
            return externalConfig.gameRulesConfig.ruleDisableRaids;
        }
        return data.ruleDisableRaids;
    }

    @Override
    public boolean getRuleDoInsomnia() {
        if (externalConfig != null) {
            return externalConfig.gameRulesConfig.ruleDoInsomnia != null ? externalConfig.gameRulesConfig.ruleDoInsomnia : false;
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
