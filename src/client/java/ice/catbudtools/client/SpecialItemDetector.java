package ice.catbudtools.client;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * Identifies CatBud special items from their vanilla item components.
 */
public final class SpecialItemDetector {
	private static final String CATBUD_ENCHANTMENT_NAMESPACE = "addons";

	private SpecialItemDetector() {
	}

	/**
	 * Returns true for an enchanted book containing any addons:* stored enchantment.
	 */
	public static boolean isSpecial(ItemStack stack) {
		if (!stack.isOf(Items.ENCHANTED_BOOK)) {
			return false;
		}

		ItemEnchantmentsComponent storedEnchantments = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
		return storedEnchantments != null
				&& storedEnchantments.getEnchantments().stream()
						.anyMatch(enchantment -> enchantment.getKey()
								.map(key -> key.getValue().getNamespace().equals(CATBUD_ENCHANTMENT_NAMESPACE))
								.orElse(false));
	}
}