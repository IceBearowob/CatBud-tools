package ice.catbudtools.client.specialtooltip;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import ice.catbudtools.CatBudTools;

public final class SpecialItemRegistry {

    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, SpecialItemInfo> SPECIAL_ITEM = new HashMap<>();
    private static boolean loaded = false;

    private SpecialItemRegistry() {}

    /**
     * 加載special_items.json
     * 通常在 {@code onInitializeClient()} 中呼叫。
     */
    public static void load() {
        SPECIAL_ITEM.clear();

        try (InputStream stream = SpecialItemRegistry.class.getResourceAsStream(
                "/assets/catbud-tools/special_items.json")) {
            if (stream != null) {
                try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    Type type = new TypeToken<Map<String, SpecialItemInfo>>() {}.getType();
                    Map<String, SpecialItemInfo> parsed = GSON.fromJson(reader, type);
                    if (parsed != null) {
                        SPECIAL_ITEM.putAll(parsed);
                    }
                }
            } else {
                CatBudTools.LOGGER.warn("[CatBud Tools] 找不到內建 special_items.json 資源檔");
            }
        } catch (Exception e) {
            CatBudTools.LOGGER.warn("[CatBud Tools] 無法從資源載入 special_items.json: " + e.getMessage());
        }

        loaded = true;
    }

    /**
     * 取得指定附魔路徑（path）的特附資料，例如 {@code "landmark"}。
     * 若尚未載入則自動呼叫 {@link #load()}。
     *
     * @param path 附魔的 path 部分（不含命名空間前綴）
     * @return 對應的 {@link SpecialItemInfo}，或 {@code null}（若不存在）
     */
    public static SpecialItemInfo get(String path) {
        if (!loaded) {
            load();
        }
        return SPECIAL_ITEM.get(path);
    }


    public static boolean has(String uniqueKey) {

        if (uniqueKey == null) {
            return false;
        }

        return uniqueKey != null && SPECIAL_ITEM.containsKey(uniqueKey);
    }

}