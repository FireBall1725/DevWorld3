package ca.fireball1725.devworld.config;

import ca.fireball1725.devworld.DevWorld;
import ca.fireball1725.devworld.dataclass.ExternalConfig;
import ca.fireball1725.devworld.dataclass.GameRulesConfig;
import ca.fireball1725.devworld.dataclass.WorldConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Shared config loader for loading external configuration from DEVWORLD_CONFIG environment variable.
 * This is used by both Fabric and NeoForge implementations.
 */
public class ExternalConfigLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ExternalConfig cachedConfig = null;
    private static boolean loadAttempted = false;

    /**
     * Get the external config, loading it if not already cached.
     * Returns null if DEVWORLD_CONFIG is not set or loading fails.
     */
    public static ExternalConfig getExternalConfig() {
        if (!loadAttempted) {
            loadAttempted = true;
            loadExternalConfig();
        }
        return cachedConfig;
    }

    /**
     * Check if an external config was successfully loaded.
     */
    public static boolean hasExternalConfig() {
        return getExternalConfig() != null;
    }

    private static void loadExternalConfig() {
        String devWorldConfigUrl = System.getenv("DEVWORLD_CONFIG");

        if (devWorldConfigUrl == null || devWorldConfigUrl.isEmpty()) {
            DevWorld.LOGGER.debug("DEVWORLD_CONFIG environment variable not set, using platform-specific config");
            return;
        }

        DevWorld.LOGGER.info("Found DEVWORLD_CONFIG environment variable: {}", devWorldConfigUrl);

        try {
            String jsonConfig = readStringFromURL(devWorldConfigUrl);
            DevWorld.LOGGER.debug("Fetched config JSON (length: {} bytes)", jsonConfig.length());

            cachedConfig = GSON.fromJson(jsonConfig, ExternalConfig.class);
            cachedConfig = setDefaultsOnNull(cachedConfig);

            DevWorld.LOGGER.info("Successfully loaded external config from URL");
            DevWorld.LOGGER.debug("External config - World preset: {}",
                cachedConfig.worldConfig.worldGenerationPreset);
        } catch (IOException e) {
            DevWorld.LOGGER.error("Failed to fetch external config from: {}", devWorldConfigUrl, e);
            DevWorld.LOGGER.info("Falling back to platform-specific config");
            cachedConfig = null;
        } catch (Exception e) {
            DevWorld.LOGGER.error("Failed to parse external config JSON", e);
            DevWorld.LOGGER.info("Falling back to platform-specific config");
            cachedConfig = null;
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
        if (config == null) {
            config = new ExternalConfig();
        }

        if (config.gameRulesConfig == null) {
            config.gameRulesConfig = new GameRulesConfig();
        }

        if (config.worldConfig == null) {
            config.worldConfig = new WorldConfig();
        }

        return config;
    }

    /**
     * Force reload the external config (useful for testing).
     */
    public static void reload() {
        loadAttempted = false;
        cachedConfig = null;
        getExternalConfig();
    }
}
