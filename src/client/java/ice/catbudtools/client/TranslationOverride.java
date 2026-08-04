package ice.catbudtools.client;

import java.util.HashMap;
import java.util.Map;

public class TranslationOverride {


    private static final Map<String, String> TRANSLATIONS =
            new HashMap<>();


    public static void clear() {
        TRANSLATIONS.clear();
    }


    public static void put(
            String key,
            String value
    ) {
        TRANSLATIONS.put(
                key,
                value
        );
    }


    public static String get(
            String key
    ) {
        return TRANSLATIONS.get(key);
    }
}