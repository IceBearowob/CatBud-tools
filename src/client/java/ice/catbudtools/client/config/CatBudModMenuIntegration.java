package ice.catbudtools.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import ice.catbudtools.client.config.yacl.YaclConfigScreen;

/**
 * ModMenu 介面整合點，提供開啟模組設定畫面的 Factory。
 */
public class CatBudModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return YaclConfigScreen::create;
    }
}
