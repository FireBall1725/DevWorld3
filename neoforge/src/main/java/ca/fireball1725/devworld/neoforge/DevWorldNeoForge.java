package ca.fireball1725.devworld.neoforge;

import ca.fireball1725.devworld.DevWorld;
import ca.fireball1725.devworld.neoforge.client.DevWorldNeoForgeClient;
import ca.fireball1725.devworld.neoforge.config.DevWorldConfigImpl;
import ca.fireball1725.devworld.server.ServerEventHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod(DevWorld.MOD_ID)
public class DevWorldNeoForge {
    public DevWorldNeoForge(ModContainer container) {
        DevWorld.init();

        // Register config
        container.registerConfig(ModConfig.Type.CLIENT, DevWorldConfigImpl.getSpec());

        // Register server events
        NeoForge.EVENT_BUS.register(new ServerEvents());

        // Initialize client if on client side
        if (FMLEnvironment.dist.isClient()) {
            DevWorldNeoForgeClient.init();
        }
    }

    private static class ServerEvents {
        @SubscribeEvent
        public void onServerStarted(ServerStartedEvent event) {
            ServerEventHandler.onServerStarted(event.getServer(), DevWorldConfigImpl.getInstance());
        }
    }
}
