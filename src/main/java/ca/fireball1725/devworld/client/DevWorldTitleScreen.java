package ca.fireball1725.devworld.client;

import ca.fireball1725.devworld.DevWorldCommon;
import ca.fireball1725.devworld.config.DevWorldConfig;
import ca.fireball1725.devworld.util.DevWorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
//? if >=1.20 && <26.1
import net.minecraft.client.gui.GuiGraphics;
//? if >=26.1
//import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if <1.20
//import com.mojang.blaze3d.vertex.PoseStack;

/** The New / Load / Delete buttons on the title screen. The loader entrypoints call into this. */
public final class DevWorldTitleScreen {
    private static final int LEFT_SHIFT = 340;
    private static final int BUTTON_WIDTH = 84;
    private static final int BUTTON_HEIGHT = 20;
    // Top left corner of the title screen, which is empty
    private static final int EXTERNAL_CONFIG_X = 4;
    private static final int EXTERNAL_CONFIG_Y = 4;

    private static final DevWorldUtils DEV_WORLD = new DevWorldUtils();

    private static Button buttonCreate, buttonLoad, buttonDelete;
    private static int keyShiftCount = 0;

    private DevWorldTitleScreen() {}

    private static Button button(int x, int y, Component label, Button.OnPress onPress) {
        //? if >=1.19.3
        return Button.builder(label, onPress).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        //? if <1.19.3
        //return new Button(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, label, onPress);
    }

    /** Called when the title screen initialises; adds the buttons through {@code add}. */
    public static void init(TitleScreen screen, Consumer<AbstractWidget> add) {
        int buttonY = screen.height / 4 + 48;
        int buttonX = screen.width / 2 + 104;

        buttonCreate = button(buttonX, buttonY, Component.translatable("devworld.menu.new"), b -> {
            try {
                DEV_WORLD.createDevWorld();
            } catch (Exception ex) {
                DevWorldCommon.LOGGER.error("Could not create the DevWorld", ex);
            }
        });

        buttonLoad = button(buttonX, buttonY, Component.translatable("devworld.menu.load"),
                b -> DEV_WORLD.loadDevWorld());

        buttonDelete = button(buttonX, buttonY + 24, Component.translatable("devworld.menu.delete"), b -> {
            DEV_WORLD.deleteDevWorld();
            keyShiftCount = 0;
        });

        buttonCreate.visible = false;
        buttonLoad.visible = false;
        buttonDelete.visible = false;
        buttonDelete.active = false;
        keyShiftCount = 0;

        add.accept(buttonCreate);
        add.accept(buttonLoad);
        add.accept(buttonDelete);
    }

    /** Called after a key press on the title screen; two left shift presses enable the delete button. */
    public static void keyPressed(int keyCode) {
        if (keyCode == LEFT_SHIFT) {
            keyShiftCount++;
        }

        if (keyShiftCount >= 2 && buttonDelete != null) {
            buttonDelete.active = true;
        }
    }

    private static void updateVisibility() {
        boolean exists = DEV_WORLD.saveExists();
        buttonCreate.visible = !exists;
        buttonDelete.visible = exists;
        buttonLoad.visible = exists;
    }

    private static int titleX(TitleScreen screen) {
        return screen.width / 2 + 104 + (BUTTON_WIDTH / 2);
    }

    private static int titleY(TitleScreen screen) {
        return screen.height / 4 + 38;
    }

    /** The line in the top left corner about the {@code DEVWORLD_CONFIG} config, or null when it is not in use. */
    private static Component externalConfigLine() {
        if (DevWorldConfig.externalFailed()) {
            return Component.translatable("devworld.config.external_failed");
        }
        String host = DevWorldConfig.externalHost();
        return host == null ? null : Component.translatable("devworld.config.external", host);
    }

    private static int externalConfigColor() {
        return DevWorldConfig.externalFailed() ? 0xFFFF5555 : 0xFF55FF55;
    }

    private static boolean showDeleteHint() {
        return buttonDelete.isHoveredOrFocused() && buttonDelete.visible && !buttonDelete.active;
    }

    //? if >=26.1 {
    /*/^* Called at the end of the title screen render; draws the mod name and the delete hint. ^/
    public static void render(TitleScreen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (buttonCreate == null) {
            return;
        }
        updateVisibility();

        graphics.centeredText(Minecraft.getInstance().font, Component.translatable("devworld.title"),
                titleX(screen), titleY(screen), 0xFFFFFFFF);

        Component external = externalConfigLine();
        if (external != null) {
            graphics.text(Minecraft.getInstance().font, external, EXTERNAL_CONFIG_X, EXTERNAL_CONFIG_Y,
                    externalConfigColor());
        }

        if (showDeleteHint()) {
            graphics.setTooltipForNextFrame(Minecraft.getInstance().font,
                    Component.translatable("devworld.hover.delete"), mouseX, mouseY);
        }
    }
    *///?}
    //? if >=1.20 && <26.1 {
    /** Called at the end of the title screen render; draws the mod name and the delete hint. */
    public static void render(TitleScreen screen, GuiGraphics graphics, int mouseX, int mouseY) {
        if (buttonCreate == null) {
            return;
        }
        updateVisibility();

        graphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable("devworld.title"),
                titleX(screen), titleY(screen), 0xFFFFFFFF);

        Component external = externalConfigLine();
        if (external != null) {
            graphics.drawString(Minecraft.getInstance().font, external, EXTERNAL_CONFIG_X, EXTERNAL_CONFIG_Y,
                    externalConfigColor());
        }

        if (showDeleteHint()) {
            graphics.renderTooltip(Minecraft.getInstance().font, Component.translatable("devworld.hover.delete"),
                    mouseX, mouseY);
        }
    }
    //?}
    //? if <1.20 {
    /*public static void render(TitleScreen screen, PoseStack poseStack, int mouseX, int mouseY) {
        if (buttonCreate == null) {
            return;
        }
        updateVisibility();

        net.minecraft.client.gui.GuiComponent.drawCenteredString(poseStack, Minecraft.getInstance().font,
                Component.translatable("devworld.title"), titleX(screen), titleY(screen), 0xFFFFFFFF);

        Component external = externalConfigLine();
        if (external != null) {
            Minecraft.getInstance().font.drawShadow(poseStack, external, EXTERNAL_CONFIG_X, EXTERNAL_CONFIG_Y,
                    externalConfigColor());
        }

        if (showDeleteHint()) {
            screen.renderTooltip(poseStack, Component.translatable("devworld.hover.delete"), mouseX, mouseY);
        }
    }
    *///?}
}
