package ca.fireball1725.devworld.util;

//? if >=26.1 {
/*import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.gamerules.GameRules;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameRuleApplier26Test {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static JsonObject json(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }

    private static GameRules rules() {
        return new GameRules(FeatureFlags.DEFAULT_FLAGS);
    }

    @Test
    void translatesTheOldNamesToTheNewRules() {
        GameRules rules = rules();
        assertTrue(rules.get(GameRules.ADVANCE_TIME));

        GameRuleApplier.apply(rules,
                json("{\"doDaylightCycle\": false, \"doMobSpawning\": false, \"mobGriefing\": false}"));

        assertFalse(rules.get(GameRules.ADVANCE_TIME));
        assertFalse(rules.get(GameRules.SPAWN_MOBS));
        assertFalse(rules.get(GameRules.MOB_GRIEFING));
    }

    @Test
    void invertsTheRulesThatChangedMeaning() {
        GameRules rules = rules();
        assertTrue(rules.get(GameRules.RAIDS));

        GameRuleApplier.apply(rules, json("{\"disableRaids\": true}"));

        assertFalse(rules.get(GameRules.RAIDS));
    }

    @Test
    void doFireTickIsTheSpreadRadius() {
        GameRules rules = rules();

        GameRuleApplier.apply(rules, json("{\"doFireTick\": false}"));

        assertEquals(0, rules.get(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER));
    }

    @Test
    void acceptsTheNewNamesAndTheSnakeCasedOldOnes() {
        GameRules rules = rules();

        GameRuleApplier.apply(rules, json("{\"random_tick_speed\": 7, \"keepInventory\": true}"));

        assertEquals(7, rules.get(GameRules.RANDOM_TICK_SPEED));
        assertTrue(rules.get(GameRules.KEEP_INVENTORY));
    }

    @Test
    void leavesARuleAloneWhenTheValueIsTheWrongTypeOrTheRuleIsUnknown() {
        GameRules rules = rules();

        GameRuleApplier.apply(rules, json("{\"doDaylightCycle\": \"yes\", \"notARule\": true}"));

        assertTrue(rules.get(GameRules.ADVANCE_TIME));
    }
}
*///?}
