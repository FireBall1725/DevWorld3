package ca.fireball1725.devworld.client.events;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.slf4j.Logger;

public class DevWorldClientEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    private Button buttonCreate;
    private Button buttonLoad;
    private Button buttonDelete;
    private int keyShiftCount = 0;

    public DevWorldClientEvents() {
        MinecraftForge.EVENT_BUS.addListener(this::renderMainMenu);
        MinecraftForge.EVENT_BUS.addListener(this::initMainMenu);
    }

    public void renderMainMenu(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof TitleScreen titleScreen) {
            buttonCreate.visible = true;
        }
    }

    public void initMainMenu(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof TitleScreen titleScreen) {
            int buttonY = event.getScreen().height / 4 + 48;
            int buttonX = event.getScreen().width / 2 + 104;

            buttonCreate = new Button(buttonX, buttonY, 84, 20, Component.translatable("devworld.menu.new"), button -> {
                LOGGER.info(">>> Create new devworld");
                titleScreen.renderables.clear();
            });
            buttonLoad = new Button(buttonX, buttonY, 40, 20, Component.translatable("devworld.menu.load"), button -> {
                LOGGER.info(">>> Load devworld");
            });
            buttonX += 44;
            buttonDelete = new Button(buttonX, buttonY, 40, 20, Component.translatable("devworld.menu.delete"), button -> {
                LOGGER.info(">>> Delete devworld");
                keyShiftCount = 0;
            });

            buttonCreate.visible = false;
            buttonLoad.visible = false;
            buttonDelete.visible = false;
            buttonDelete.active = false;

            keyShiftCount = 0;

            titleScreen.renderables.add(buttonCreate);
            titleScreen.renderables.add(buttonLoad);
            titleScreen.renderables.add(buttonDelete);
        }
    }
}
