package ca.fireball1725.devworld;

import ca.fireball1725.devworld.config.DevWorldConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import org.slf4j.Logger;

public final class DevWorldCommon {
    public static final String MOD_ID = "devworld";
    public static final Logger LOGGER = LogUtils.getLogger();

    private DevWorldCommon() {}

    /** Sets the time of day when the daylight cycle is off, as the integrated server comes up. */
    public static void onServerStarted(MinecraftServer server) {
        // Only the dev world; other singleplayer worlds keep their own time
        if (!DevWorldConfig.world().worldName.equals(server.getWorldData().getLevelName())) {
            return;
        }
        if (server.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            return;
        }

        ServerLevel level = server.overworld();
        int time = DevWorldConfig.world().timeOfDay;
        if (level.getDayTime() != time) {
            level.setDayTime(time);
        }
    }
}
