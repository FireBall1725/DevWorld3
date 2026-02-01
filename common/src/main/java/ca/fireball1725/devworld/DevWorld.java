package ca.fireball1725.devworld;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class DevWorld {
    public static final String MOD_ID = "devworld";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        LOGGER.info("DevWorld initializing on platform: {}",
            DevWorldExpectPlatform.getPlatformName());
    }
}
