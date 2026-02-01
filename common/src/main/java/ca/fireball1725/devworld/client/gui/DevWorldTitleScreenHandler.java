package ca.fireball1725.devworld.client.gui;

import ca.fireball1725.devworld.DevWorldExpectPlatform;
import ca.fireball1725.devworld.util.DevWorldUtils;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

public class DevWorldTitleScreenHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DevWorldUtils devWorldUtils;
    private Button buttonCreate, buttonLoad, buttonDelete;
    private int keyShiftCount = 0;

    public DevWorldTitleScreenHandler() {
        this.devWorldUtils = new DevWorldUtils(DevWorldExpectPlatform.getConfig());
    }

    public void onScreenInit(Screen screen, ButtonAdder buttonAdder) {
        if (!(screen instanceof TitleScreen)) return;

        int buttonY = screen.height / 4 + 48;
        int buttonX = screen.width / 2 + 104;

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

        buttonCreate.visible = false;
        buttonLoad.visible = false;
        buttonDelete.visible = false;
        buttonDelete.active = false;
        keyShiftCount = 0;

        buttonAdder.addButton(buttonCreate);
        buttonAdder.addButton(buttonLoad);
        buttonAdder.addButton(buttonDelete);
    }

    public void onKeyPressed(Screen screen, int keyCode) {
        if (!(screen instanceof TitleScreen)) return;

        if (keyCode == 340) { // Left Shift
            keyShiftCount++;
            if (keyShiftCount >= 2 && buttonDelete != null) {
                buttonDelete.active = true;
            }
        }
    }

    public void onRenderPost(Screen screen, RenderContext ctx) {
        if (!(screen instanceof TitleScreen titleScreen)) return;

        if (devWorldUtils.saveExists()) {
            buttonCreate.visible = false;
            buttonDelete.visible = true;
            buttonLoad.visible = true;
        } else {
            buttonCreate.visible = true;
            buttonDelete.visible = false;
            buttonLoad.visible = false;
        }

        int textY = screen.height / 4 + 38;
        int textX = screen.width / 2 + 104 + 42;

        ctx.drawCenteredString(
            Component.translatable("devworld.title"),
            textX,
            textY
        );

        if (buttonDelete != null && buttonDelete.isHoveredOrFocused() && buttonDelete.visible && !buttonDelete.active) {
            ctx.renderTooltip(
                Component.translatable("devworld.hover.delete"),
                ctx.mouseX,
                ctx.mouseY
            );
        }
    }

    @FunctionalInterface
    public interface ButtonAdder {
        void addButton(Button button);
    }

    public static class RenderContext {
        public final net.minecraft.client.gui.GuiGraphics guiGraphics;
        public final int mouseX;
        public final int mouseY;

        public RenderContext(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY) {
            this.guiGraphics = guiGraphics;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }

        public void drawCenteredString(Component text, int x, int y) {
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, text, x, y, 16777215);
        }

        public void renderTooltip(Component text, int x, int y) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, text, x, y);
        }
    }
}
