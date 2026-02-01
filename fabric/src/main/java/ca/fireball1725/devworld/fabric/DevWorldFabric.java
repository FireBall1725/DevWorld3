package ca.fireball1725.devworld.fabric;

import ca.fireball1725.devworld.DevWorld;
import ca.fireball1725.devworld.fabric.config.DevWorldConfigImpl;
import ca.fireball1725.devworld.server.ServerEventHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class DevWorldFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DevWorld.init();

        // Load config
        DevWorldConfigImpl.load();

        // Register server started event
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerEventHandler.onServerStarted(server, DevWorldConfigImpl.getInstance());
        });
    }
}
