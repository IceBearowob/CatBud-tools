package ice.catbudtools.client;

import java.util.HashSet;
import java.util.Set;

public final class SpecialItemRegistry {

    private static final Set<String> ITEMS = new HashSet<>();


    static {

        register(
                "humanoid_armor_stand_spirit_enthusiastic"
        );

    }


    private static void register(String uniqueKey) {
        ITEMS.add(uniqueKey);
    }


    public static boolean has(String uniqueKey) {
        return ITEMS.contains(uniqueKey);
    }


    private SpecialItemRegistry() {
    }
}