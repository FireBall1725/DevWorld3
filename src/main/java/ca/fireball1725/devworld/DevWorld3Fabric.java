package ca.fireball1725.devworld;

//? if fabric {
import ca.fireball1725.devworld.client.DevWorldTitleScreen;
import ca.fireball1725.devworld.config.DevWorldConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.TitleScreen;

public class DevWorld3Fabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DevWorldConfig.load(FabricLoader.getInstance().getConfigDir());

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen title) {
                //? if <26.1
                DevWorldTitleScreen.init(title, Screens.getButtons(screen)::add);
                //? if >=26.1
                //DevWorldTitleScreen.init(title, Screens.getWidgets(screen)::add);

                //? if >=26.1 {
                /*ScreenKeyboardEvents.afterKeyPress(screen)
                        .register((s, event) -> DevWorldTitleScreen.keyPressed(event.key()));
                ScreenEvents.afterExtract(screen)
                        .register((s, graphics, mouseX, mouseY, tickDelta) ->
                                DevWorldTitleScreen.render(title, graphics, mouseX, mouseY));
                *///?}
                //? if <26.1 {
                ScreenKeyboardEvents.afterKeyPress(screen)
                        .register((s, key, scancode, modifiers) -> DevWorldTitleScreen.keyPressed(key));
                ScreenEvents.afterRender(screen)
                        .register((s, graphics, mouseX, mouseY, tickDelta) ->
                                DevWorldTitleScreen.render(title, graphics, mouseX, mouseY));
                //?}
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(DevWorldCommon::onServerStarted);
    }
}
//?}
