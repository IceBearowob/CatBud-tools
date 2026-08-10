package ice.catbudtools.client;

import java.util.Set;

public final class SpecialItemRegistry {


    private static final Set<String> PREFIXES = Set.of(

            "humanoid_armor_stand",
            "mimicry_card.",
            "catnip",
            "player_doll",
            "enderman_exile_core",
            "power_disable_alarm",
            "power_enable_alarm",
            "armor_stand_",
            "item_frame_transparent_masking",
            "command_proxy",
            "slient_mod_needle",
            "landmark",
            "arrow"

    );


    public static boolean has(String uniqueKey) {

        if (uniqueKey == null) {
            return false;
        }


        return PREFIXES.stream()
                .anyMatch(uniqueKey::startsWith);
    }


    private SpecialItemRegistry() {
    }
}