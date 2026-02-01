package ca.fireball1725.devworld.forge;

import ca.fireball1725.devworld.DevWorld;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(DevWorld.MOD_ID)
public class DevWorldForge {
    public DevWorldForge() {
        DevWorld.init();

        if (FMLEnvironment.dist.isClient()) {
            DevWorldForgeClient.init();
        }
    }
}
