package ice.catbudtools.client.specialtooltip;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ice.catbudtools.CatBudTools;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 從 assets/catbud-tools/special_enchants.json 載入特附（特殊附魔）資料的 Registry。
 * 結構與 CatBudCommandRegistry 相同，採用懶加載並支援在 onInitializeClient 時預先載入。
 */
public final class SpecialEnchantRegistry {

    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, SpecialEnchantInfo> ENCHANTS = new HashMap<>();
    private static boolean loaded = false;

    private SpecialEnchantRegistry() {}

    /**
     * 從內建資源載入 special_enchants.json。
     * 通常在 {@code onInitializeClient()} 中呼叫。
     */
    public static void load() {
        ENCHANTS.clear();

        try (InputStream stream = SpecialEnchantRegistry.class.getResourceAsStream(
                "/assets/catbud-tools/special_enchants.json")) {
            if (stream != null) {
                try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    Type type = new TypeToken<Map<String, SpecialEnchantInfo>>() {}.getType();
                    Map<String, SpecialEnchantInfo> parsed = GSON.fromJson(reader, type);
                    if (parsed != null) {
                        ENCHANTS.putAll(parsed);
                    }
                }
            } else {
                CatBudTools.LOGGER.warn("[CatBud Tools] 找不到內建 special_enchants.json 資源檔");
            }
        } catch (Exception e) {
            CatBudTools.LOGGER.warn("[CatBud Tools] 無法從資源載入 special_enchants.json: " + e.getMessage());
        }

        loaded = true;
    }

    /**
     * 取得指定附魔路徑（path）的特附資料，例如 {@code "auto_repair"}。
     * 若尚未載入則自動呼叫 {@link #load()}。
     *
     * @param path 附魔的 path 部分（不含命名空間前綴）
     * @return 對應的 {@link SpecialEnchantInfo}，或 {@code null}（若不存在）
     */
    public static SpecialEnchantInfo get(String path) {
        if (!loaded) {
            load();
        }
        return ENCHANTS.get(path);
    }
}
