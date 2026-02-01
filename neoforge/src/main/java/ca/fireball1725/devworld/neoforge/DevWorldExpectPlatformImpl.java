package ca.fireball1725.devworld.neoforge;

import ca.fireball1725.devworld.config.DevWorldConfig;
import ca.fireball1725.devworld.neoforge.config.DevWorldConfigImpl;
import net.neoforged.fml.loading.FMLEnvironment;

public class DevWorldExpectPlatformImpl {
    public static String getPlatformName() {
        return "NeoForge";
    }

    public static boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }

    public static DevWorldConfig getConfig() {
        return DevWorldConfigImpl.getInstance();
    }
}
