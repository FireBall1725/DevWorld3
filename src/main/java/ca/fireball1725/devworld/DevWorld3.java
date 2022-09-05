package ca.fireball1725.devworld;

import ca.fireball1725.devworld.client.events.DevWorldClientEvents;
import ca.fireball1725.devworld.config.Config;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(DevWorld3.MOD_ID)
public class DevWorld3 {
    public static final String MOD_ID = "devworld";
    private final Logger LOGGER = LogUtils.getLogger();

    public DevWorld3() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> DevWorldClientEvents::new);

        // Check to see if there is user configuration
        String devWorldConfig = System.getenv("DEVWORLD_CONFIG2");
        LOGGER.info(">>> Dev World Configuration: " + devWorldConfig);
        LOGGER.info(">>> " + (devWorldConfig == null));

        Config.register();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientStartup(FMLClientSetupEvent event) {

        }
    }
}
