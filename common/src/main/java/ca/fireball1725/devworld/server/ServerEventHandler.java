package ca.fireball1725.devworld.server;

import ca.fireball1725.devworld.config.DevWorldConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public class ServerEventHandler {
    public static void onServerStarted(MinecraftServer server, DevWorldConfig config) {
        ServerLevel overworld = server.overworld();

        if (!config.getRuleDaylight()) {
            int time = config.getDaylightValue();
            if (overworld.getGameTime() != time) {
                overworld.setDayTime(time);
            }
        }
    }
}
