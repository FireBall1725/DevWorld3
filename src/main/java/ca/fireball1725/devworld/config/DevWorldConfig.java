package ca.fireball1725.devworld.config;

import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class DevWorldConfig {
    public static ForgeConfigSpec.ConfigValue<String> FLATWORLD_GENERATOR_STRING;

    public static void registerClientConfig(ForgeConfigSpec.Builder CLIENT_BUILDER) {
        CLIENT_BUILDER.comment("Client settings for the power generator").push("world_config");

        FLATWORLD_GENERATOR_STRING = CLIENT_BUILDER
                .comment("Flat world generator preset")
                .define("world_generation_preset", "minecraft:bedrock,3*minecraft:stone,116*minecraft:sandstone;minecraft:desert");

        CLIENT_BUILDER.pop();
    }
}
