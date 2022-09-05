package ca.fireball1725.devworld.dataclass;

import com.google.gson.annotations.SerializedName;

public class ExternalConfig {
    @SerializedName("world_config")
    public WorldConfig worldConfig;

    @SerializedName("game_rules")
    public GameRulesConfig gameRulesConfig;
}