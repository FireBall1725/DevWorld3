package ca.fireball1725.devworld.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Config {
    public static void register() {
        registerClientConfigs();
    }

    private static void registerClientConfigs() {
        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
        DevWorldConfig.registerClientConfig(CLIENT_BUILDER);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_BUILDER.build());
    }
}
