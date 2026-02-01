package ca.fireball1725.devworld.client.events;

import ca.fireball1725.devworld.config.DevWorldConfig;
import ca.fireball1725.devworld.util.DevWorldUtils;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;

/*? if forge {*/
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
/*?} else {*/
/*? if neoforge {*/
/*import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
/*?} else */
/*import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.gui.GuiGraphics;
/*?}*/
/*?}*/

public class DevWorldClientEvents /*? if fabric {*//*implements ClientModInitializer*//*?}*/ {
    private static final Logger LOGGER = LogUtils.getLogger();

    private Button buttonCreate, buttonLoad, buttonDelete;
    private int keyShiftCount = 0;
    private DevWorldUtils devWorldUtils;

    /*? if fabric {*/
    /*@Override
    public void onInitializeClient() {
        devWorldUtils = new DevWorldUtils();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen) {
                eventScreenInit(screen, scaledWidth, scaledHeight);

                ScreenKeyboardEvents.afterKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
                    eventScreenKeyPressed(screen1, key);
                });
            }
        });

        ScreenEvents.AFTER_RENDER.register((screen, guiGraphics, mouseX, mouseY, tickDelta) -> {
            if (screen instanceof TitleScreen titleScreen) {
                eventScreenRender(screen, guiGraphics, mouseX, mouseY);
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(this::eventServerStarted);
    }
    /*?} else */
    public DevWorldClientEvents() {
        devWorldUtils = new DevWorldUtils();

        /*? if forge {*/
        MinecraftForge.EVENT_BUS.addListener(this::eventScreenKeyPressedPost);
        MinecraftForge.EVENT_BUS.addListener(this::eventScreenRenderPost);
        MinecraftForge.EVENT_BUS.addListener(this::eventScreenInit);
        MinecraftForge.EVENT_BUS.addListener(this::eventServerStarted);
        /*?} else {*/
/*? if neoforge {*/
        /*NeoForge.EVENT_BUS.addListener(this::eventScreenKeyPressedPost);
        NeoForge.EVENT_BUS.addListener(this::eventScreenRenderPost);
        NeoForge.EVENT_BUS.addListener(this::eventScreenInit);
        NeoForge.EVENT_BUS.addListener(this::eventServerStarted);
        /*?}*/
    }
    /*?}*/

    /*? if forge {*/
    public void eventScreenKeyPressedPost(ScreenEvent.KeyPressed.Post event) {
        if (event.getScreen() instanceof TitleScreen) {
            if (event.getKeyCode() == 340) {
                keyShiftCount++;
            }
            if (keyShiftCount >= 2) {
                buttonDelete.active = true;
            }
        }
    }

    public void eventScreenRenderPost(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof TitleScreen titleScreen) {
            updateButtonVisibility();

            int textY = event.getScreen().height / 4 + 38;
            int textX = event.getScreen().width / 2 + 104 + 42;

            PoseStack poseStack = new PoseStack();
            GuiComponent.drawCenteredString(
                    poseStack,
                    Minecraft.getInstance().font,
                    Component.translatable("devworld.title"),
                    textX,
                    textY,
                    16777215
            );

            if (buttonDelete.isHoveredOrFocused() && buttonDelete.visible && !buttonDelete.active) {
                titleScreen.renderTooltip(
                        poseStack,
                        Component.translatable("devworld.hover.delete"),
                        event.getMouseX(),
                        event.getMouseY()
                );
            }
        }
    }

    public void eventScreenInit(ScreenEvent.Init event) {
        if (event.getScreen() instanceof TitleScreen) {
            initButtons(event.getScreen(), event.getScreen().width, event.getScreen().height);
            event.addListener(buttonCreate);
            event.addListener(buttonLoad);
            event.addListener(buttonDelete);
        }
    }

    public void eventServerStarted(ServerStartedEvent event) {
        handleServerStarted(event.getServer().overworld());
    }
    /*?} else {*/
/*? if neoforge {*/
    /*public void eventScreenKeyPressedPost(ScreenEvent.KeyPressed.Post event) {
        if (event.getScreen() instanceof TitleScreen) {
            if (event.getKeyCode() == 340) {
                keyShiftCount++;
            }
            if (keyShiftCount >= 2) {
                buttonDelete.active = true;
            }
        }
    }

    public void eventScreenRenderPost(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof TitleScreen titleScreen) {
            updateButtonVisibility();

            int textY = event.getScreen().height / 4 + 38;
            int textX = event.getScreen().width / 2 + 104 + 42;

            event.getGuiGraphics().drawCenteredString(
                    Minecraft.getInstance().font,
                    Component.translatable("devworld.title"),
                    textX,
                    textY,
                    16777215
            );

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

    public void eventScreenInit(ScreenEvent.Init event) {
        if (event.getScreen() instanceof TitleScreen) {
            initButtons(event.getScreen(), event.getScreen().width, event.getScreen().height);
            event.addListener(buttonCreate);
            event.addListener(buttonLoad);
            event.addListener(buttonDelete);
        }
    }

    public void eventServerStarted(ServerStartedEvent event) {
        handleServerStarted(event.getServer().overworld());
    }
    /*?} else */
    /*private void eventScreenKeyPressed(net.minecraft.client.gui.screens.Screen screen, int key) {
        if (key == 340) {
            keyShiftCount++;
        }
        if (keyShiftCount >= 2 && buttonDelete != null) {
            buttonDelete.active = true;
        }
    }

    private void eventScreenRender(net.minecraft.client.gui.screens.Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        updateButtonVisibility();

        int textY = screen.height / 4 + 38;
        int textX = screen.width / 2 + 104 + 42;

        guiGraphics.drawCenteredString(
                Minecraft.getInstance().font,
                Component.translatable("devworld.title"),
                textX,
                textY,
                16777215
        );

        if (buttonDelete != null && buttonDelete.isHoveredOrFocused() && buttonDelete.visible && !buttonDelete.active) {
            guiGraphics.renderTooltip(
                    Minecraft.getInstance().font,
                    Component.translatable("devworld.hover.delete"),
                    mouseX,
                    mouseY
            );
        }
    }

    private void eventScreenInit(net.minecraft.client.gui.screens.Screen screen, int width, int height) {
        initButtons(screen, width, height);
        ScreenEvents.addButton(screen, buttonCreate);
        ScreenEvents.addButton(screen, buttonLoad);
        ScreenEvents.addButton(screen, buttonDelete);
    }

    private void eventServerStarted(net.minecraft.server.MinecraftServer server) {
        handleServerStarted(server.overworld());
    }
    /*?}*/
    /*?}*/

    private void initButtons(net.minecraft.client.gui.screens.Screen screen, int width, int height) {
        int buttonY = height / 4 + 48;
        int buttonX = width / 2 + 104;

        /*? if >=1.20 {*/
        buttonCreate = Button.builder(
                Component.translatable("devworld.menu.new"),
                button -> {
                    try {
                        devWorldUtils.createDevWorld();
                    } catch (Exception ex) {
                        LOGGER.error("Failed to create dev world", ex);
                    }
                }
        ).bounds(buttonX, buttonY, 84, 20).build();

        buttonLoad = Button.builder(
                Component.translatable("devworld.menu.load"),
                button -> devWorldUtils.loadDevWorld()
        ).bounds(buttonX, buttonY, 84, 20).build();

        buttonY += 24;

        buttonDelete = Button.builder(
                Component.translatable("devworld.menu.delete"),
                button -> {
                    devWorldUtils.deleteDevWorld();
                    keyShiftCount = 0;
                }
        ).bounds(buttonX, buttonY, 84, 20).build();
        /*?} else */
        /*buttonCreate = new Button(
                buttonX,
                buttonY,
                84,
                20,
                Component.translatable("devworld.menu.new"),
                button -> {
                    try {
                        devWorldUtils.createDevWorld();
                    } catch (Exception ex) {
                        LOGGER.error("Failed to create dev world", ex);
                    }
                }
        );

        buttonLoad = new Button(
                buttonX,
                buttonY,
                84,
                20,
                Component.translatable("devworld.menu.load"),
                button -> devWorldUtils.loadDevWorld()
        );

        buttonY += 24;

        buttonDelete = new Button(
                buttonX,
                buttonY,
                84,
                20,
                Component.translatable("devworld.menu.delete"),
                button -> {
                    devWorldUtils.deleteDevWorld();
                    keyShiftCount = 0;
                }
        );
        /*?}*/

        buttonCreate.visible = false;
        buttonLoad.visible = false;
        buttonDelete.visible = false;
        buttonDelete.active = false;
        keyShiftCount = 0;
    }

    private void updateButtonVisibility() {
        if (devWorldUtils.saveExists()) {
            buttonCreate.visible = false;
            buttonDelete.visible = true;
            buttonLoad.visible = true;
        } else {
            buttonCreate.visible = true;
            buttonDelete.visible = false;
            buttonLoad.visible = false;
        }
    }

    private void handleServerStarted(ServerLevel serverLevel) {
        if (!DevWorldConfig.RULE_DAYLIGHT.get()) {
            int time = DevWorldConfig.DAYLIGHT_VALUE.get();
            if (serverLevel.getGameTime() != time) {
                serverLevel.setDayTime(time);
            }
        }
    }
}
