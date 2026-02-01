package ca.fireball1725.devworld.dataclass;

import com.google.gson.annotations.SerializedName;

public class WorldConfig {
    @SerializedName("world_generation_preset")
    public String worldGenerationPreset = "minecraft:bedrock,3*minecraft:stone,116*minecraft:sandstone;minecraft:desert";

    @SerializedName("bonus_chest")
    public Boolean bonusChest = false;
}
