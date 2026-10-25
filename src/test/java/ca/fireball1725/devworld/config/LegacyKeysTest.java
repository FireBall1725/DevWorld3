package ca.fireball1725.devworld.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyKeysTest {
    private static JsonObject json(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }

    @Test
    void renamesOldRuleKeysToVanillaNames() {
        JsonObject root = json("""
                {"game_rules": {"rule_daylight": true, "rule_weather_cycle": false, "rule_dofiretick": true,
                  "rule_mobgriefing": true, "rule_domobspawning": false, "rule_disable_raids": false,
                  "rule_doinsomnia": true}}""");

        LegacyKeys.migrate(root);

        JsonObject rules = root.getAsJsonObject("game_rules");
        assertTrue(rules.get("doDaylightCycle").getAsBoolean());
        assertFalse(rules.get("doWeatherCycle").getAsBoolean());
        assertTrue(rules.get("doFireTick").getAsBoolean());
        assertTrue(rules.get("mobGriefing").getAsBoolean());
        assertFalse(rules.get("doMobSpawning").getAsBoolean());
        assertFalse(rules.get("disableRaids").getAsBoolean());
        assertTrue(rules.get("doInsomnia").getAsBoolean());
        assertEquals(7, rules.size());
    }

    @Test
    void acceptsTheMisspeltInsomniaKeyFromTheOldExternalFormat() {
        JsonObject root = json("{\"game_rules\": {\"rule_do_insomnia\": true}}");

        LegacyKeys.migrate(root);

        assertTrue(root.getAsJsonObject("game_rules").get("doInsomnia").getAsBoolean());
    }

    @Test
    void movesDaylightTimeIntoWorldConfig() {
        JsonObject root = json("{\"game_rules\": {\"daylight_time\": 18000}}");

        LegacyKeys.migrate(root);

        assertNull(root.getAsJsonObject("game_rules").get("daylight_time"));
        assertEquals(18000, root.getAsJsonObject("world_config").get("time_of_day").getAsInt());
    }

    @Test
    void keepsAnExistingTimeOfDay() {
        JsonObject root = json(
                "{\"world_config\": {\"time_of_day\": 1000}, \"game_rules\": {\"daylight_time\": 18000}}");

        LegacyKeys.migrate(root);

        assertEquals(1000, root.getAsJsonObject("world_config").get("time_of_day").getAsInt());
    }

    @Test
    void vanillaNameWinsOverItsAlias() {
        JsonObject root = json("{\"game_rules\": {\"rule_daylight\": true, \"doDaylightCycle\": false}}");

        LegacyKeys.migrate(root);

        JsonObject rules = root.getAsJsonObject("game_rules");
        assertFalse(rules.get("doDaylightCycle").getAsBoolean());
        assertFalse(rules.has("rule_daylight"));
    }

    @Test
    void leavesFilesWithoutGameRulesAlone() {
        JsonObject root = json("{\"world_config\": {\"seed\": 5}}");

        LegacyKeys.migrate(root);
        LegacyKeys.migrate(null);

        assertEquals("{\"world_config\":{\"seed\":5}}", root.toString());
    }
}
