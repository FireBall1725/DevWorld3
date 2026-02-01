package ca.fireball1725.devworld;

import ca.fireball1725.devworld.config.DevWorldConfig;
import dev.architectury.injectables.annotations.ExpectPlatform;

public class DevWorldExpectPlatform {
    @ExpectPlatform
    public static String getPlatformName() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isClient() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static DevWorldConfig getConfig() {
        throw new AssertionError();
    }
}
