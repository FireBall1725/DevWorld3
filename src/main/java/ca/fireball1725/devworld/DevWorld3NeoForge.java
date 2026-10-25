package ca.fireball1725.devworld;

//? if neoforge {
/*import ca.fireball1725.devworld.client.DevWorldTitleScreen;
import ca.fireball1725.devworld.config.DevWorldConfig;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod(value = DevWorldCommon.MOD_ID, dist = Dist.CLIENT)
public class DevWorld3NeoForge {
    public DevWorld3NeoForge() {
        DevWorldConfig.load(FMLPaths.CONFIGDIR.get());

        NeoForge.EVENT_BUS.addListener((ScreenEvent.Init.Post event) -> {
            if (event.getScreen() instanceof TitleScreen title) {
                DevWorldTitleScreen.init(title, event::addListener);
            }
        });
        NeoForge.EVENT_BUS.addListener((ScreenEvent.KeyPressed.Post event) -> {
            if (event.getScreen() instanceof TitleScreen) {
                DevWorldTitleScreen.keyPressed(event.getKeyCode());
            }
        });
        NeoForge.EVENT_BUS.addListener((ScreenEvent.Render.Post event) -> {
            if (event.getScreen() instanceof TitleScreen title) {
                DevWorldTitleScreen.render(title, event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
            }
        });
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) ->
                DevWorldCommon.onServerStarted(event.getServer()));
    }
}
*///?}
