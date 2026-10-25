package ca.fireball1725.devworld.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

/** Rewrites the game rule keys used before the config took vanilla rule names, so older files keep working. */
final class LegacyKeys {
    private static final Map<String, String> GAME_RULES = Map.of(
            "rule_daylight", "doDaylightCycle",
            "rule_weather_cycle", "doWeatherCycle",
            "rule_dofiretick", "doFireTick",
            "rule_mobgriefing", "mobGriefing",
            "rule_domobspawning", "doMobSpawning",
            "rule_disable_raids", "disableRaids",
            "rule_doinsomnia", "doInsomnia",
            "rule_do_insomnia", "doInsomnia"
    );

    private LegacyKeys() {}

    /** Renames the old keys in place; a vanilla name that is already present wins over its old alias. */
    static void migrate(JsonObject root) {
        if (root == null || !root.has("game_rules") || !root.get("game_rules").isJsonObject()) {
            return;
        }

        JsonObject rules = root.getAsJsonObject("game_rules");
        for (var alias : GAME_RULES.entrySet()) {
            JsonElement value = rules.remove(alias.getKey());
            if (value != null && !rules.has(alias.getValue())) {
                rules.add(alias.getValue(), value);
            }
        }

        // daylight_time was a game rule entry; it is the world's time of day now
        JsonElement time = rules.remove("daylight_time");
        if (time != null) {
            if (!root.has("world_config") || !root.get("world_config").isJsonObject()) {
                root.add("world_config", new JsonObject());
            }
            JsonObject world = root.getAsJsonObject("world_config");
            if (!world.has("time_of_day")) {
                world.add("time_of_day", time);
            }
        }
    }
}
