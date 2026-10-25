package ca.fireball1725.devworld.dataclass;

import com.google.gson.annotations.SerializedName;

public class WorldConfig {
    @SerializedName("world_name")
    public String worldName = "DevWorld";

    @SerializedName("world_generation_preset")
    public String worldGenerationPreset = "minecraft:bedrock,3*minecraft:stone,116*minecraft:sandstone;minecraft:desert";

    @SerializedName("bonus_chest")
    public Boolean bonusChest = false;

    /** survival, creative, adventure or spectator */
    @SerializedName("game_mode")
    public String gameMode = "creative";

    /** peaceful, easy, normal or hard */
    @SerializedName("difficulty")
    public String difficulty = "normal";

    @SerializedName("seed")
    public Long seed = 0L;

    @SerializedName("generate_structures")
    public Boolean generateStructures = false;

    @SerializedName("allow_commands")
    public Boolean allowCommands = true;

    /** The time of day the world is set to when it opens, if the daylight cycle is off. */
    @SerializedName("time_of_day")
    public Integer timeOfDay = 6000;
}
