package ice.catbudtools.client.lang;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ice.catbudtools.CatBudTools;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 從伺服器資源包載入 zh_tw.json，提供簡單的 key-value 查詢。
 */
public final class ServerLangRegistry {
    private static final Gson GSON = new Gson();
    private static final Map<String, String> RAW_TRANSLATIONS = new HashMap<>();
    private static boolean loaded = false;

    private ServerLangRegistry() {}

    /**
     * 從當前套用的伺服器材質包中重新載入 zh_tw.json
     */
    public static synchronized void reload() {
        clearAll();
        Minecraft client = Minecraft.getInstance();
        PackRepository packRepository = client.getResourcePackRepository();

        boolean foundServerPack = false;

        for (Pack pack : packRepository.getSelectedPacks()) {
            if (pack.getPackSource() == PackSource.SERVER) {
                try (PackResources packResources = pack.open()) {
                    foundServerPack = true;
                    for (String namespace : packResources.getNamespaces(PackType.CLIENT_RESOURCES)) {
                        Identifier langId = Identifier.fromNamespaceAndPath(namespace, "lang/zh_tw.json");
                        IoSupplier<InputStream> streamSupplier = packResources.getResource(PackType.CLIENT_RESOURCES, langId);

                        if (streamSupplier != null) {
                            try (InputStream stream = streamSupplier.get();
                                 InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                                Map<String, String> parsed = GSON.fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
                                if (parsed != null) {
                                    RAW_TRANSLATIONS.putAll(parsed);
                                    CatBudTools.LOGGER.info("[CatBud Tools] 從伺服器材質包載入 {} 個語言條目 ({})", parsed.size(), namespace);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    CatBudTools.LOGGER.warn("[CatBud Tools] 讀取伺服器材質包語言檔出錯: " + e.getMessage());
                }
            }
        }

        if (foundServerPack && !RAW_TRANSLATIONS.isEmpty()) {
            CatBudTools.LOGGER.info("[CatBud Tools] 伺服器語言載入完成，共 {} 個條目", RAW_TRANSLATIONS.size());
        }

        loaded = true;
    }

    public static synchronized void clearAll() {
        RAW_TRANSLATIONS.clear();
        loaded = false;
    }

    private static void ensureLoaded() {
        if (!loaded) {
            reload();
        }
    }

    // ==========================================
    // 查詢 API
    // ==========================================

    /**
     * 用完整 key 取得翻譯值
     */
    public static String getRaw(String key) {
        ensureLoaded();
        return key != null ? RAW_TRANSLATIONS.get(key) : null;
    }

    /**
     * 檢查某個 key 是否存在
     */
    public static boolean hasRaw(String key) {
        ensureLoaded();
        return key != null && RAW_TRANSLATIONS.containsKey(key);
    }

    /**
     * 以 key 結尾（suffix）搜尋，回傳第一個匹配的完整 key。
     * 用於 plugin 前綴不固定的情況（如 buff、config）。
     */
    public static String findKeyBySuffix(String suffix) {
        ensureLoaded();
        if (suffix == null) return null;
        for (String key : RAW_TRANSLATIONS.keySet()) {
            if (key.endsWith(suffix)) return key;
        }
        return null;
    }
}
