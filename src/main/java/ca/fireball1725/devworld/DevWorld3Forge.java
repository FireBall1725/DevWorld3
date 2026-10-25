package ca.fireball1725.devworld;

//? if forge {
/*import ca.fireball1725.devworld.client.DevWorldTitleScreen;
import ca.fireball1725.devworld.config.DevWorldConfig;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(DevWorldCommon.MOD_ID)
public class DevWorld3Forge {
    public DevWorld3Forge() {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }

        DevWorldConfig.load(FMLPaths.CONFIGDIR.get());

        MinecraftForge.EVENT_BUS.addListener((ScreenEvent.Init.Post event) -> {
            if (event.getScreen() instanceof TitleScreen title) {
                DevWorldTitleScreen.init(title, event::addListener);
            }
        });
        MinecraftForge.EVENT_BUS.addListener((ScreenEvent.KeyPressed.Post event) -> {
            if (event.getScreen() instanceof TitleScreen) {
                DevWorldTitleScreen.keyPressed(event.getKeyCode());
            }
        });
        MinecraftForge.EVENT_BUS.addListener((ScreenEvent.Render.Post event) -> {
            if (event.getScreen() instanceof TitleScreen title) {
                //? if >=1.20
                DevWorldTitleScreen.render(title, event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
                //? if <1.20
                //DevWorldTitleScreen.render(title, event.getPoseStack(), event.getMouseX(), event.getMouseY());
            }
        });
        MinecraftForge.EVENT_BUS.addListener((ServerStartedEvent event) ->
                DevWorldCommon.onServerStarted(event.getServer()));
    }
}
*///?}
