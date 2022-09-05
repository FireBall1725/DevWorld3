package ca.fireball1725.devworld.config;

import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class DevWorldConfig {
    public static ForgeConfigSpec.ConfigValue<String> WORLDGEN_STRING;

    public static ForgeConfigSpec.ConfigValue<List<String>> ADDITIONAL_STRUCTURE_SETS;


    public static void registerClientConfig(ForgeConfigSpec.Builder CLIENT_BUILDER) {
        CLIENT_BUILDER.comment("Client settings for the power generator").push("powergen");


        WORLDGEN_STRING = CLIENT_BUILDER
                .comment("World generation string")
                .define("generator_string", "hello");

        ADDITIONAL_STRUCTURE_SETS = CLIENT_BUILDER
                .comment("Test")
                .define("test", List.of("Hello", "world"));


        CLIENT_BUILDER.pop();
    }
}
