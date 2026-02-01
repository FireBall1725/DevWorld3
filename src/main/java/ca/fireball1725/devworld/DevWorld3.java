package ca.fireball1725.devworld;

import ca.fireball1725.devworld.client.events.DevWorldClientEvents;
import ca.fireball1725.devworld.config.Config;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/*? if forge {*/
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
/*?} else {*/
/*? if neoforge {*/
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
/*?} else */
/*import net.fabricmc.api.ModInitializer;
/*?}*/
/*?}*/

/*? if forgeLike {*/
@Mod(DevWorld3.MOD_ID)
/*?}*/
public class DevWorld3 /*? if fabric {*//*implements ModInitializer*//*?}*/ {
    public static final String MOD_ID = "devworld";
    private static final Logger LOGGER = LogUtils.getLogger();

    /*? if forge {*/
    public DevWorld3() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> DevWorldClientEvents::new);
        Config.register();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("DevWorld3 common setup");
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientStartup(FMLClientSetupEvent event) {
            LOGGER.info("DevWorld3 client setup");
        }
    }
    /*?} else {*/
/*? if neoforge {*/
    /*public DevWorld3(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            new DevWorldClientEvents();
        }

        Config.register();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("DevWorld3 common setup");
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientStartup(FMLClientSetupEvent event) {
            LOGGER.info("DevWorld3 client setup");
        }
    }
    /*?} else */
    /*@Override
    public void onInitialize() {
        LOGGER.info("DevWorld3 initializing on Fabric");
        Config.register();
    }
    /*?}*/
    /*?}*/
}
