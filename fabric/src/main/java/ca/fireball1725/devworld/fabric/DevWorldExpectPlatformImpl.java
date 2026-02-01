package ca.fireball1725.devworld.fabric;

import ca.fireball1725.devworld.config.DevWorldConfig;
import ca.fireball1725.devworld.fabric.config.DevWorldConfigImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class DevWorldExpectPlatformImpl {
    public static String getPlatformName() {
        return "Fabric";
    }

    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static DevWorldConfig getConfig() {
        return DevWorldConfigImpl.getInstance();
    }
}
