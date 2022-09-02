package ca.fireball1725.devworld.client.events;

import ca.fireball1725.devworld.util.DevWorldUtils;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.WorldStem;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.core.pattern.PlainTextRenderer;
import org.slf4j.Logger;

public class DevWorldClientEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    private Button buttonCreate;
    private Button buttonLoad;
    private Button buttonDelete;
    private int keyShiftCount = 0;

    public DevWorldClientEvents() {
        MinecraftForge.EVENT_BUS.addListener(this::keyPress);
        MinecraftForge.EVENT_BUS.addListener(this::renderMainMenu);
        MinecraftForge.EVENT_BUS.addListener(this::initMainMenu);
    }

    public void keyPress(ScreenEvent.KeyPressed.Post event) {
        if (event.getScreen() instanceof TitleScreen titleScreen) {
            LOGGER.info(">>> Key Press: " + event.getKeyCode());

            if (event.getKeyCode() == 340)
                keyShiftCount ++;

            if (keyShiftCount >= 2)
                buttonDelete.active = true;
        }
    }

    public void renderMainMenu(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof TitleScreen titleScreen) {
            buttonDelete.visible = true;
            buttonCreate.visible = true;

            //GuiComponent.drawCenteredString();
        }
    }

    public void initMainMenu(ScreenEvent.Init event) {
        if (event.getScreen() instanceof TitleScreen titleScreen) {
            int buttonY = event.getScreen().height / 4 + 38;
            int buttonX = event.getScreen().width / 2 + 104;

            PlainTextButton devWorldText = new PlainTextButton(buttonX, buttonY, 84, 20, Component.literal("DevWorld 3"), button -> {}, Minecraft.getInstance().font);


            buttonY += 10;
            buttonCreate = new Button(buttonX, buttonY, 84, 20, Component.translatable("devworld.menu.new"), button -> {
                try {
                    DevWorldUtils devWorldUtils = new DevWorldUtils();
                    devWorldUtils.createDevWorld();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                }
            });

            buttonLoad = new Button(buttonX, buttonY, 84, 20, Component.translatable("devworld.menu.load"), button -> {
                LOGGER.info(">>> Load devworld");
            });

            buttonY += 24;
            buttonDelete = new Button(buttonX, buttonY, 84, 20, Component.translatable("devworld.menu.delete"), button -> {
                LOGGER.info(">>> Delete devworld");
                keyShiftCount = 0;
            });

            buttonCreate.visible = false;
            buttonLoad.visible = false;
            buttonDelete.visible = false;
            buttonDelete.active = false;

            keyShiftCount = 0;

            event.addListener(devWorldText);
            event.addListener(buttonCreate);
            event.addListener(buttonLoad);
            event.addListener(buttonDelete);
        }
    }
}
