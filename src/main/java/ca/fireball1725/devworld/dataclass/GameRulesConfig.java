package ca.fireball1725.devworld.dataclass;

import com.google.gson.annotations.SerializedName;

public class GameRulesConfig {
    @SerializedName("rule_daylight")
    public Boolean ruleDayLightCycle = false;

    @SerializedName("daylight_time")
    public Integer daylightTime = 6000;

    @SerializedName("rule_weather_cycle")
    public Boolean ruleWeatherCycle = false;

    @SerializedName("rule_dofiretick")
    public Boolean ruleDoFireTick = false;

    @SerializedName("rule_mobgriefing")
    public Boolean ruleMobGriefing = false;

    @SerializedName("rule_domobspawning")
    public Boolean ruleDoMobSpawning = true;

    @SerializedName("rule_disable_raids")
    public boolean ruleDisableRaids = true;

    @SerializedName("rule_do_insomnia")
    public Boolean ruleDoInsomnia = false;
}
