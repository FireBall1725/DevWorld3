package ca.fireball1725.devworld.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** The few Minecraft calls that moved between the versions this mod supports. */
public final class Compat {
    private Compat() {}

    public static Screen currentScreen(Minecraft mc) {
        //? if >=26.2
        //return mc.gui.screen();
        //? if <26.2
        return mc.screen;
    }

    public static void setScreen(Minecraft mc, Screen screen) {
        //? if >=26.1
        //mc.setScreenAndShow(screen);
        //? if <26.1
        mc.setScreen(screen);
    }
}
