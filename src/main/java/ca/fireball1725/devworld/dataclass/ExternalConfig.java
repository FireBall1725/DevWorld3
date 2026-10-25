package ca.fireball1725.devworld.dataclass;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class ExternalConfig {
    @SerializedName("world_config")
    public WorldConfig worldConfig;

    /** Vanilla game rule names mapped to their values, for example {@code "doDaylightCycle": false}. */
    @SerializedName("game_rules")
    public JsonObject gameRules;
}
