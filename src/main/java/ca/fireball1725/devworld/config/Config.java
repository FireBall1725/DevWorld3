package ca.fireball1725.devworld.config;

import ca.fireball1725.devworld.dataclass.ExternalConfig;
import ca.fireball1725.devworld.dataclass.GameRulesConfig;
import ca.fireball1725.devworld.dataclass.WorldConfig;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Config {
    public static void register() {
        getExternalConfigs();
        registerClientConfigs();
    }
    private static final Logger LOGGER = LogUtils.getLogger();
    public static ExternalConfig EXTERNAL_CONFIG;

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

        LOGGER.info("Hello");
    }

    private static void registerClientConfigs() {
        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
        DevWorldConfig.registerClientConfig(CLIENT_BUILDER);
        DevWorldConfig.registerGameRuleConfig(CLIENT_BUILDER);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_BUILDER.build());
    }

    private static String readStringFromURL(String requestURL) throws IOException
    {
        try (Scanner scanner = new Scanner(new URL(requestURL).openStream(),
                StandardCharsets.UTF_8.toString()))
        {
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
