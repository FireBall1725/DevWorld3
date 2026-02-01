package ca.fireball1725.devworld.forge.client;

import ca.fireball1725.devworld.client.DevWorldClient;
import ca.fireball1725.devworld.client.gui.DevWorldTitleScreenHandler;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DevWorldForgeClient {
    public static void init() {
        DevWorldClient.init();
        MinecraftForge.EVENT_BUS.register(new ScreenEventHandler());
    }

    private static class ScreenEventHandler {
        @SubscribeEvent
        public void onScreenInit(ScreenEvent.Init.Post event) {
            DevWorldClient.getTitleScreenHandler().onScreenInit(
                event.getScreen(),
                event::addListener
            );
        }

        @SubscribeEvent
        public void onKeyPressed(ScreenEvent.KeyPressed.Post event) {
            DevWorldClient.getTitleScreenHandler().onKeyPressed(
                event.getScreen(),
                event.getKeyCode()
            );
        }

        @SubscribeEvent
        public void onRenderPost(ScreenEvent.Render.Post event) {
            DevWorldClient.getTitleScreenHandler().onRenderPost(
                event.getScreen(),
                new DevWorldTitleScreenHandler.RenderContext(
                    event.getGuiGraphics(),
                    event.getMouseX(),
                    event.getMouseY()
                )
            );
        }
    }
}
