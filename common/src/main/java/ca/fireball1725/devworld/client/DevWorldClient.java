package ca.fireball1725.devworld.client;

import ca.fireball1725.devworld.DevWorld;
import ca.fireball1725.devworld.client.gui.DevWorldTitleScreenHandler;

public class DevWorldClient {
    private static DevWorldTitleScreenHandler titleScreenHandler;

    public static void init() {
        DevWorld.LOGGER.info("DevWorld client initializing");
        titleScreenHandler = new DevWorldTitleScreenHandler();
    }

    public static DevWorldTitleScreenHandler getTitleScreenHandler() {
        return titleScreenHandler;
    }
}
