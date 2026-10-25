package ca.fireball1725.devworld.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.world.level.GameRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameRuleApplierTest {
    private static JsonObject json(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }

    @Test
    void setsBooleanAndIntegerRulesByVanillaName() {
        GameRules rules = new GameRules();
        assertTrue(rules.getBoolean(GameRules.RULE_DAYLIGHT));

        GameRuleApplier.apply(rules, json("{\"doDaylightCycle\": false, \"randomTickSpeed\": 7}"));

        assertFalse(rules.getBoolean(GameRules.RULE_DAYLIGHT));
        assertEquals(7, rules.getInt(GameRules.RULE_RANDOMTICKING));
    }

    @Test
    void leavesARuleAloneWhenTheValueIsTheWrongType() {
        GameRules rules = new GameRules();

        GameRuleApplier.apply(rules, json("{\"doDaylightCycle\": \"yes\", \"randomTickSpeed\": true}"));

        assertTrue(rules.getBoolean(GameRules.RULE_DAYLIGHT));
        assertEquals(3, rules.getInt(GameRules.RULE_RANDOMTICKING));
    }

    @Test
    void skipsUnknownRules() {
        GameRules rules = new GameRules();

        GameRuleApplier.apply(rules, json("{\"notARule\": true, \"keepInventory\": true}"));

        assertTrue(rules.getBoolean(GameRules.RULE_KEEPINVENTORY));
    }
}
