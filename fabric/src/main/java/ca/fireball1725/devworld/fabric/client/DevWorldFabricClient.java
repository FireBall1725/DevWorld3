package ca.fireball1725.devworld.fabric.client;

import ca.fireball1725.devworld.client.DevWorldClient;
import ca.fireball1725.devworld.client.gui.DevWorldTitleScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;

public class DevWorldFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DevWorldClient.init();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            // Register button adder using Fabric Screen API
            DevWorldClient.getTitleScreenHandler().onScreenInit(
                screen,
                button -> Screens.getButtons(screen).add(button)
            );

            ScreenKeyboardEvents.afterKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
                DevWorldClient.getTitleScreenHandler().onKeyPressed(screen1, key);
            });

            // Register render event for this specific screen
            ScreenEvents.afterRender(screen).register((screen1, guiGraphics, mouseX, mouseY, tickDelta) -> {
                DevWorldClient.getTitleScreenHandler().onRenderPost(
                    screen1,
                    new DevWorldTitleScreenHandler.RenderContext(guiGraphics, mouseX, mouseY)
                );
            });
        });
    }
}
