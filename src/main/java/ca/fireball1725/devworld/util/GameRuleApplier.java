package ca.fireball1725.devworld.util;

import ca.fireball1725.devworld.DevWorldCommon;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.world.level.GameRules;

import java.util.HashSet;
import java.util.Set;

/** Sets game rules from a map of vanilla rule names to values. Rules from other mods work too. */
public final class GameRuleApplier {
    private GameRuleApplier() {}

    public static void apply(GameRules rules, JsonObject config) {
        Set<String> unmatched = new HashSet<>(config.keySet());

        GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            @Override
            public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                JsonElement value = config.get(key.getId());
                if (value == null) {
                    return;
                }
                unmatched.remove(key.getId());

                T rule = rules.getRule(key);
                if (rule instanceof GameRules.BooleanValue booleanRule && isBoolean(value)) {
                    booleanRule.set(value.getAsBoolean(), null);
                } else if (rule instanceof GameRules.IntegerValue integerRule && isNumber(value)) {
                    integerRule.set(value.getAsInt(), null);
                } else {
                    DevWorldCommon.LOGGER.warn("Game rule {} does not take the value {}; leaving it at the default",
                            key.getId(), value);
                }
            }
        });

        for (String name : unmatched) {
            DevWorldCommon.LOGGER.warn("Unknown game rule {} in the DevWorld config", name);
        }
    }

    private static boolean isBoolean(JsonElement value) {
        return value.isJsonPrimitive() && ((JsonPrimitive) value).isBoolean();
    }

    private static boolean isNumber(JsonElement value) {
        return value.isJsonPrimitive() && ((JsonPrimitive) value).isNumber();
    }
}
