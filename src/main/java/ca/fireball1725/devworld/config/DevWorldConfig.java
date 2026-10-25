package ca.fireball1725.devworld.config;

import ca.fireball1725.devworld.DevWorldCommon;
import ca.fireball1725.devworld.dataclass.ExternalConfig;
import ca.fireball1725.devworld.dataclass.WorldConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Loads {@code config/devworld.json}. The defaults come from the JSON at the URL in the
 * {@code DEVWORLD_CONFIG} environment variable when it is set, otherwise from the built-in ones.
 * Anything in the local file wins over both.
 */
public final class DevWorldConfig {
    private static final Logger LOGGER = DevWorldCommon.LOGGER;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static ExternalConfig config = withDefaults(new ExternalConfig());
    private static String externalHost;
    private static boolean externalFailed;

    private DevWorldConfig() {}

    public static WorldConfig world() {
        return config.worldConfig;
    }

    /** Vanilla game rule names mapped to their values. */
    public static JsonObject gameRules() {
        return config.gameRules;
    }

    /** The host the external config was loaded from, or null when it was not used or did not load. */
    public static String externalHost() {
        return externalHost;
    }

    /** Whether {@code DEVWORLD_CONFIG} is set but the config could not be loaded from it. */
    public static boolean externalFailed() {
        return externalFailed;
    }

    /** Loads the config, and writes the file with the effective values if it is missing. */
    public static void load(Path configDir) {
        load(configDir, System.getenv("DEVWORLD_CONFIG"));
    }

    /** As {@link #load(Path)}, with the external config URL passed in instead of read from the environment. */
    static void load(Path configDir, String externalUrl) {
        externalHost = null;
        externalFailed = false;
        JsonObject merged = loadDefaults(externalUrl);
        Path file = configDir.resolve("devworld.json");
        boolean writeBack = true;

        if (Files.exists(file)) {
            try {
                JsonObject local = GSON.fromJson(Files.readString(file), JsonObject.class);
                LegacyKeys.migrate(local);
                merge(merged, local);
            } catch (IOException | JsonParseException ex) {
                // Leave a hand-edited file that does not parse alone, so it is not overwritten
                LOGGER.error("Could not read {}; using the defaults", file, ex);
                writeBack = false;
            }
        }

        config = withDefaults(GSON.fromJson(merged, ExternalConfig.class));

        if (writeBack) {
            try {
                Files.createDirectories(configDir);
                String json = GSON.toJson(config);
                if (!Files.exists(file) || !json.equals(Files.readString(file))) {
                    Files.writeString(file, json);
                }
            } catch (IOException ex) {
                LOGGER.error("Could not write {}", file, ex);
            }
        }
    }

    /** The built-in defaults, with the external config over them when {@code DEVWORLD_CONFIG} is set. */
    private static JsonObject loadDefaults(String url) {
        JsonObject defaults = GSON.toJsonTree(withDefaults(new ExternalConfig())).getAsJsonObject();

        if (url == null || url.isBlank()) {
            return defaults;
        }

        try {
            JsonObject external = GSON.fromJson(readStringFromURL(url), JsonObject.class);
            LegacyKeys.migrate(external);
            merge(defaults, external);
            String host = URI.create(url).getHost();
            externalHost = host == null || host.isEmpty() ? url : host;
        } catch (IOException | JsonParseException | IllegalArgumentException ex) {
            LOGGER.error("Could not load the external configuration from {}", url, ex);
            externalFailed = true;
        }
        return defaults;
    }

    /** Copies every key of {@code over} into {@code base}, going into nested objects. */
    private static void merge(JsonObject base, JsonObject over) {
        if (over == null) {
            return;
        }
        for (var entry : over.entrySet()) {
            JsonElement existing = base.get(entry.getKey());
            if (existing != null && existing.isJsonObject() && entry.getValue().isJsonObject()) {
                merge(existing.getAsJsonObject(), entry.getValue().getAsJsonObject());
            } else {
                base.add(entry.getKey(), entry.getValue());
            }
        }
    }

    private static String readStringFromURL(String requestURL) throws IOException {
        try (Scanner scanner = new Scanner(URI.create(requestURL).toURL().openStream(), StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    private static JsonObject defaultGameRules() {
        JsonObject rules = new JsonObject();
        rules.addProperty("doDaylightCycle", false);
        rules.addProperty("doWeatherCycle", false);
        rules.addProperty("doFireTick", false);
        rules.addProperty("mobGriefing", false);
        rules.addProperty("doMobSpawning", true);
        rules.addProperty("disableRaids", true);
        rules.addProperty("doInsomnia", false);
        return rules;
    }

    private static ExternalConfig withDefaults(ExternalConfig loaded) {
        ExternalConfig result = loaded == null ? new ExternalConfig() : loaded;
        if (result.gameRules == null) {
            result.gameRules = defaultGameRules();
        }
        if (result.worldConfig == null) {
            result.worldConfig = new WorldConfig();
        }
        return result;
    }
}
