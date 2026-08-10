package ice.catbudtools.client.specialtooltip;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;


public final class SpecialItemDetector {

    private static final String CATBUD_ENCHANTMENT_NAMESPACE = "addons";

    private SpecialItemDetector() {
    }


    // ==========================
    // 特殊附魔
    // ==========================

    public static boolean isSpecialEnchant(ItemStack stack) {

        ItemEnchantmentsComponent storedEnchantments = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);

        return storedEnchantments != null
                && storedEnchantments.getEnchantments().stream()
                .anyMatch(enchantment -> enchantment.getKey()
                        .map(key -> key.getValue().getNamespace().equals(CATBUD_ENCHANTMENT_NAMESPACE))
                        .orElse(false));
    }
	public static boolean isSpecialAppliedEnchant(ItemStack stack) {

		ItemEnchantmentsComponent enchantments =
				stack.get(DataComponentTypes.ENCHANTMENTS);

		return enchantments != null
				&& enchantments.getEnchantments().stream()
				.anyMatch(enchantment -> enchantment.getKey()
						.map(key -> key.getValue()
								.getNamespace()
								.equals(CATBUD_ENCHANTMENT_NAMESPACE))
						.orElse(false));
	}

    // ==========================
    // 取得特殊物品 UniqueKey
    // ==========================

	public static String getSpecialItemId(ItemStack stack) {

		NbtComponent customData =
				stack.get(DataComponentTypes.CUSTOM_DATA);

		if (customData == null) {
			return null;
		}

		NbtCompound root =
				customData.copyNbt();

		if (!root.contains("PublicBukkitValues")) {
			return null;
		}

		NbtCompound publicValues =
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