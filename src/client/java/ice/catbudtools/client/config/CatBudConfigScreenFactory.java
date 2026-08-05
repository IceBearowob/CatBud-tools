package ice.catbudtools.client.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;


public class CatBudConfigScreenFactory {


    public static Screen create(Screen parent) {

        if (FabricLoader.getInstance()
                .isModLoaded("yet_another_config_lib_v3")) {

            try {
                Class<?> clazz = Class.forName("ice.catbudtools.client.config.yacl.YaclConfigScreen");
                return (Screen) clazz.getMethod("create",Screen.class).invoke(null, parent);
            } catch (Exception e) {
                throw new RuntimeException(
                    "Failed to open YACL config screen",
                    e
                );
            }
        }
        return new CatBudConfigScreen(parent);
    }
}