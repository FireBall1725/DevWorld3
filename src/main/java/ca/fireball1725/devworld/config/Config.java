package ca.fireball1725.devworld.config;

import ca.fireball1725.devworld.dataclass.ExternalConfig;
import ca.fireball1725.devworld.dataclass.GameRulesConfig;
import ca.fireball1725.devworld.dataclass.WorldConfig;
import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/*? if forge {*/
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
/*?} elif neoforge {*/
/*import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
/*?} elif fabric {*/
/*import net.fabricmc.loader.api.FabricLoader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
/*?}*/

public class Config {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static ExternalConfig EXTERNAL_CONFIG;

    public static void register() {
        getExternalConfigs();
        registerClientConfigs();
    }

    private static void getExternalConfigs() {
        String devWorldConfig = System.getenv("DEVWORLD_CONFIG");

        if (devWorldConfig == null) {
            EXTERNAL_CONFIG = setDefaultsOnNull(new ExternalConfig());
            return;
        }

        try {
            String jsonConfig = readStringFromURL(devWorldConfig);
            Gson gson = new Gson();
            EXTERNAL_CONFIG = setDefaultsOnNull(gson.fromJson(jsonConfig, ExternalConfig.class));
        } catch (IOException ex) {
            LOGGER.error("Exception while downloading external configuration from " + devWorldConfig + ".");
            LOGGER.error(ex.getMessage());
            EXTERNAL_CONFIG = setDefaultsOnNull(new ExternalConfig());
        }

        LOGGER.info("DevWorld Config Loaded");
    }

    private static void registerClientConfigs() {
        /*? if forge {*/
        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
        DevWorldConfig.registerClientConfig(CLIENT_BUILDER);
        DevWorldConfig.registerGameRuleConfig(CLIENT_BUILDER);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_BUILDER.build());
        /*?} elif neoforge {*/
        /*ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();
        DevWorldConfig.registerClientConfig(CLIENT_BUILDER);
        DevWorldConfig.registerGameRuleConfig(CLIENT_BUILDER);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_BUILDER.build());
        /*?} elif fabric {*/
        /*// Fabric uses JSON config loaded/saved via DevWorldConfig
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("devworld.json");
        try {
            if (!Files.exists(configPath)) {
                DevWorldConfig.saveConfig(configPath);
            }
            DevWorldConfig.loadConfig(configPath);
        } catch (IOException ex) {
            LOGGER.error("Failed to load Fabric config", ex);
            DevWorldConfig.initDefaults();
        }
        /*?}*/
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
}
