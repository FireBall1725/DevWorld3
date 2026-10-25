package ca.fireball1725.devworld;

import ca.fireball1725.devworld.config.DevWorldConfig;
//? if >=26.1
//import ca.fireball1725.devworld.util.GameRuleApplier;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
//? if <26.1
import net.minecraft.world.level.GameRules;
//? if >=26.1
//import net.minecraft.world.level.gamerules.GameRules;
import org.slf4j.Logger;

public final class DevWorldCommon {
    public static final String MOD_ID = "devworld";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** Set when a new dev world is being created, so its game rules are applied once its server is up (26.1 and up). */
    public static volatile boolean applyRulesOnStart;

    private DevWorldCommon() {}

    /** Sets the time of day when the daylight cycle is off, as the integrated server comes up. */
    public static void onServerStarted(MinecraftServer server) {
        // Only the dev world; other singleplayer worlds keep their own time
        if (!DevWorldConfig.world().worldName.equals(server.getWorldData().getLevelName())) {
            return;
        }
        //? if >=26.1 {
        /*if (applyRulesOnStart) {
            applyRulesOnStart = false;
            GameRuleApplier.apply(server.getGameRules(), DevWorldConfig.gameRules());
        }
        if (server.getGameRules().get(GameRules.ADVANCE_TIME)) {
            return;
        }
        *///?}
        //? if <26.1 {
        if (server.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            return;
        }
        //?}

        ServerLevel level = server.overworld();
        int time = DevWorldConfig.world().timeOfDay;
        //? if >=26.1 {
        /*// Same as /time set: the overworld's default clock
        level.dimensionType().defaultClock().ifPresent(clock -> {
            //? if >=26.3
            //if (server.clockManager().getInstance(clock).totalTicks() != time) {
            //? if <26.3
            if (server.clockManager().getTotalTicks(clock) != time) {
                server.clockManager().setTotalTicks(clock, time);
            }
        });
        *///?}
        //? if <26.1 {
        if (level.getDayTime() != time) {
            level.setDayTime(time);
        }
        //?}
    }
}
