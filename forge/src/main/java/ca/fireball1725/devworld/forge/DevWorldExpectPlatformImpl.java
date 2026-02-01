package ca.fireball1725.devworld.forge;

import net.minecraftforge.fml.loading.FMLEnvironment;

public class DevWorldExpectPlatformImpl {
    public static String getPlatformName() {
        return "Forge";
    }

    public static boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }
}
