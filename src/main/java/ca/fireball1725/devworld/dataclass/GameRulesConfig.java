package ca.fireball1725.devworld.dataclass;

import com.google.gson.annotations.SerializedName;

public class GameRulesConfig {
    @SerializedName("daylight_cycle")
    public Boolean daylightCycle = false;

    @SerializedName("daylight_time")
    public Integer daylightTime = 6000;

    @SerializedName("weather_cycle")
    public Boolean weatherCycle = false;
}
