package ice.catbudtools.client.specialtooltip;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.ItemEnchantments;


public final class SpecialDetector {

    private static final String CATBUD_ENCHANTMENT_NAMESPACE = "addons";

    private SpecialDetector() {
    }


    // ==========================
    // 特殊附魔
    // ==========================

    public static boolean isSpecialEnchant(ItemStack stack) {

        ItemEnchantments storedEnchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);

        return storedEnchantments != null
                && storedEnchantments.keySet().stream()
                .anyMatch(enchantment -> enchantment.unwrapKey()
                        .map(key -> key.identifier().getNamespace().equals(CATBUD_ENCHANTMENT_NAMESPACE))
                        .orElse(false));
    }
	public static boolean isSpecialAppliedEnchant(ItemStack stack) {

		ItemEnchantments enchantments =
				stack.get(DataComponents.ENCHANTMENTS);

		return enchantments != null
				&& enchantments.keySet().stream()
				.anyMatch(enchantment -> enchantment.unwrapKey()
						.map(key -> key.identifier().getNamespace().equals(CATBUD_ENCHANTMENT_NAMESPACE))
						.orElse(false));
	}

    // ==========================
    // 取得特殊物品 UniqueKey
    // ==========================

	public static String getSpecialItemId(ItemStack stack) {

		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);

		if (customData == null) {
			return null;
		}

		CompoundTag root =
				customData.copyTag();

		if (!root.contains("PublicBukkitValues")) {
			return null;
		}

		CompoundTag publicValues =
				root.getCompound("PublicBukkitValues").orElse(null);

		if (!publicValues.contains("UniqueKey")) {
			return null;
		}

		return publicValues.getString("UniqueKey").orElse(null);
	}



    // ==========================
    // 是否需要 Tooltip
    // ==========================

    public static boolean hasSpecialItemTooltip(ItemStack stack) {

        String id = getSpecialItemId(stack);

        return id != null
                && SpecialItemRegistry.has(id);
    }
}