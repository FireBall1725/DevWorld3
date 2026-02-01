package ca.fireball1725.devworld.client.events;

import ca.fireball1725.devworld.config.Config;
import ca.fireball1725.devworld.config.DevWorldConfig;
import ca.fireball1725.devworld.util.DevWorldUtils;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;

public class DevWorldClientEvents {
    private final Logger LOGGER = LogUtils.getLogger();

    private Button buttonCreate, buttonLoad, buttonDelete;
    private int keyShiftCount = 0;

    private DevWorldUtils devWorldUtils;

    /**
     * Dev World client event constructor
     * This registers all the events with the forge event bus
     */
    public DevWorldClientEvents() {
        MinecraftForge.EVENT_BUS.addListener(this::eventScreenKeyPressedPost);
        MinecraftForge.EVENT_BUS.addListener(this::eventScreenRenderPost);
        MinecraftForge.EVENT_BUS.addListener(this::eventScreenInit);
        MinecraftForge.EVENT_BUS.addListener(this::eventServerStarted);

        devWorldUtils = new DevWorldUtils();
    }

    /**
     * This event gets fired at the end of a key press
     * @param event ScreenEvent.KeyPressed.Post object
     */
    public void eventScreenKeyPressedPost(ScreenEvent.KeyPressed.Post event) {
        if (event.getScreen() instanceof TitleScreen) {
            if (event.getKeyCode() == 340) // Left Shift
                keyShiftCount ++;

            // If the left shift key has been pushed 2 or more times, then enable the delete button
            if (keyShiftCount >= 2)
                buttonDelete.active = true;
        }
    }

    /**
     * This event gets fired at the end of the screen render
     * @param event ScreenEvent.Render.Post object
     */
    public void eventScreenRenderPost(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof TitleScreen titleScreen) {
            if (devWorldUtils.saveExists()) {
                buttonCreate.visible = false;
                buttonDelete.visible = true;
                buttonLoad.visible = true;
            } else {
                buttonCreate.visible = true;
                buttonDelete.visible = false;
                buttonLoad.visible = false;
            }

            // Set the initial x and y positions for the text
            int textY = event.getScreen().height / 4 + 38;
            int textX = event.getScreen().width / 2 + 104 + (84 / 2);

            // Render the mod name on the screen using GuiGraphics from event
            event.getGuiGraphics().drawCenteredString(
                    Minecraft.getInstance().font,
                    Component.translatable("devworld.title"),
                    textX,
                    textY,
                    16777215
            );

            // Render tooltip if over the delete button
            if (buttonDelete.isHoveredOrFocused() && buttonDelete.visible && !buttonDelete.active) {
                event.getGuiGraphics().renderTooltip(
                        Minecraft.getInstance().font,
                        Component.translatable("devworld.hover.delete"),
                        event.getMouseX(),
                        event.getMouseY()
                );
            }
        }
    }

    /**
     * This event gets fired when a screen initialises
     * @param event ScreenEvent.Init.Post object
     */
    public void eventScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof TitleScreen) {
            // Set the initial x and y positions
            int buttonY = event.getScreen().height / 4 + 48;
            int buttonX = event.getScreen().width / 2 + 104;

            // Create the create dev world button using Button.builder
            buttonCreate = Button.builder(
                    Component.translatable("devworld.menu.new"),
                    button -> {
                        try {
                            devWorldUtils.createDevWorld();
                        } catch (Exception ex) {
                            LOGGER.error(ex.getMessage());
                        }
                    }
            ).bounds(buttonX, buttonY, 84, 20).build();

            // Create the load dev world button using Button.builder
            buttonLoad = Button.builder(
                    Component.translatable("devworld.menu.load"),
                    button -> devWorldUtils.loadDevWorld()
            ).bounds(buttonX, buttonY, 84, 20).build();

            buttonY += 24;

            // Create the delete dev world button using Button.builder
            buttonDelete = Button.builder(
                    Component.translatable("devworld.menu.delete"),
                    button -> {
                        devWorldUtils.deleteDevWorld();
                        keyShiftCount = 0;
                    }
            ).bounds(buttonX, buttonY, 84, 20).build();

            // Set all the buttons to invisible
            buttonCreate.visible = false;
            buttonLoad.visible = false;
            buttonDelete.visible = false;

            // Set the delete button to inactive
            buttonDelete.active = false;

            // Set the shift counter to 0
            keyShiftCount = 0;

            // Add the screen objects
            event.addListener(buttonCreate);
            event.addListener(buttonLoad);
            event.addListener(buttonDelete);
        }
    }

    /**
     * This event gets fired when the embedded server is started
     * @param event ServerStartedEvent object
     */
    public void eventServerStarted(ServerStartedEvent event) {
        ServerLevel serverLevel = event.getServer().overworld();

        if (!DevWorldConfig.RULE_DAYLIGHT.get()) {
            // Check to see if the current time is noon, and if it isn't set the time to noon
            int time = DevWorldConfig.DAYLIGHT_VALUE.get();
            if (serverLevel.getGameTime() != time)
                event.getServer().overworld().setDayTime(time);
        }
    }
}
