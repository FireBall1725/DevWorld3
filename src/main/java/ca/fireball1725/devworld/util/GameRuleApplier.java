package ca.fireball1725.devworld.util;

import ca.fireball1725.devworld.DevWorldCommon;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
//? if <26.1
import net.minecraft.world.level.GameRules;
//? if >=26.1 {
/*import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
*///?}

//? if <26.1 {
import java.util.HashSet;
import java.util.Set;
//?}

/**
 * Sets game rules from a map of rule names to values. Rules from other mods work too. From 26.1 the rules are named
 * differently and a few changed meaning, so the names the config has always used are translated.
 */
public final class GameRuleApplier {
    private GameRuleApplier() {}

    private static boolean isBoolean(JsonElement value) {
        return value.isJsonPrimitive() && ((JsonPrimitive) value).isBoolean();
    }

    private static boolean isNumber(JsonElement value) {
        return value.isJsonPrimitive() && ((JsonPrimitive) value).isNumber();
    }

    //? if <26.1 {
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
    //?}
    //? if >=26.1 {
    /*// The old name of a rule, mapped to its new name and whether the new rule means the opposite
    private record Renamed(String id, boolean inverted) {}

    private static final Map<String, Renamed> RENAMED = Map.ofEntries(
            Map.entry("doDaylightCycle", new Renamed("advance_time", false)),
            Map.entry("doWeatherCycle", new Renamed("advance_weather", false)),
            Map.entry("doMobSpawning", new Renamed("spawn_mobs", false)),
            Map.entry("doInsomnia", new Renamed("spawn_phantoms", false)),
            Map.entry("doPatrolSpawning", new Renamed("spawn_patrols", false)),
            Map.entry("doTraderSpawning", new Renamed("spawn_wandering_traders", false)),
            Map.entry("doWardenSpawning", new Renamed("spawn_wardens", false)),
            Map.entry("disableRaids", new Renamed("raids", true)),
            Map.entry("doTileDrops", new Renamed("block_drops", false)),
            Map.entry("doMobLoot", new Renamed("mob_drops", false)),
            Map.entry("doEntityDrops", new Renamed("entity_drops", false)),
            Map.entry("doVinesSpread", new Renamed("spread_vines", false)),
            Map.entry("announceAdvancements", new Renamed("show_advancement_messages", false)),
            Map.entry("disableElytraMovementCheck", new Renamed("elytra_movement_check", true)),
            Map.entry("disablePlayerMovementCheck", new Renamed("player_movement_check", true))
    );

    // Turns keepInventory into keep_inventory, and doImmediateRespawn into immediate_respawn
    private static String snakeCase(String name) {
        String withoutDo = name.startsWith("do") && name.length() > 2 && Character.isUpperCase(name.charAt(2))
                ? Character.toLowerCase(name.charAt(2)) + name.substring(3)
                : name;
        return withoutDo.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
    }

    public static void apply(GameRules rules, JsonObject config) {
        Map<String, GameRule<?>> byId = new HashMap<>();
        rules.availableRules().forEach(rule -> byId.put(rule.id(), rule));

        for (var entry : config.entrySet()) {
            String name = entry.getKey();
            JsonElement value = entry.getValue();

            // doFireTick became a spread radius around players; 0 stops fire spreading, the default lets it
            if (name.equals("doFireTick")) {
                if (isBoolean(value)) {
                    set(rules, byId.get("fire_spread_radius_around_player"), name, value.getAsBoolean() ? 128 : 0);
                }
                continue;
            }

            Renamed renamed = RENAMED.get(name);
            String id = renamed != null ? renamed.id() : snakeCase(name);
            GameRule<?> rule = byId.get(id);
            if (rule == null) {
                rule = byId.get(name);
            }

            if (renamed != null && renamed.inverted() && isBoolean(value)) {
                set(rules, rule, name, !value.getAsBoolean());
            } else if (isBoolean(value)) {
                set(rules, rule, name, value.getAsBoolean());
            } else if (isNumber(value)) {
                set(rules, rule, name, value.getAsInt());
            } else {
                DevWorldCommon.LOGGER.warn("Game rule {} does not take the value {}", name, value);
            }
        }
    }

    private static void set(GameRules rules, GameRule<?> rule, String name, Object value) {
        if (rule == null) {
            DevWorldCommon.LOGGER.warn("Unknown game rule {} in the DevWorld config", name);
        } else if (!rule.valueClass().isInstance(value)) {
            DevWorldCommon.LOGGER.warn("Game rule {} does not take the value {}; leaving it at the default",
                    name, value);
        } else {
            setUnchecked(rules, rule, value);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void setUnchecked(GameRules rules, GameRule<T> rule, Object value) {
        rules.set(rule, (T) value, null);
    }
    *///?}
}
