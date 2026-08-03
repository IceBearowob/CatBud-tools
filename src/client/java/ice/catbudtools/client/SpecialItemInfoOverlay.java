package ice.catbudtools.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Keeps track of the special item currently under the cursor and renders its information card.
 */
public final class SpecialItemInfoOverlay {
	private static final String CATBUD_ENCHANTMENT_NAMESPACE = "addons";
	private static ItemStack hoveredStack = ItemStack.EMPTY;
	private static boolean observedThisFrame;

	private SpecialItemInfoOverlay() {
	}

	public static void beginFrame() {
		observedThisFrame = false;
	}

	public static void observe(ItemStack stack) {
		hoveredStack = stack.copy();
		observedThisFrame = true;
	}

	public static void render(DrawContext context, int mouseX, int mouseY) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (!observedThisFrame || hoveredStack.isEmpty() || client.getWindow() == null) {
			return;
		}

		long windowHandle = client.getWindow().getHandle();
		if (GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_G) != GLFW.GLFW_PRESS) {
			return;
		}

		List<Text> lines = getInfoLines(hoveredStack);
		if (lines.isEmpty()) {
			return;
		}

		var textRenderer = client.textRenderer;
		int width = lines.stream().mapToInt(textRenderer::getWidth).max().orElse(0) + 12;
		int height = lines.size() * 11 + 8;
		int x = mouseX + 14;
		int y = mouseY + 14;

		context.fill(x - 4, y - 4, x + width, y + height, 0xE0101010);
		for (int index = 0; index < lines.size(); index++) {
			int color = index == 0 ? 0xFFFFD85A : (index == 1 ? 0xFFFFFFFF : 0xFFB8B8B8);
			context.drawTextWithShadow(textRenderer, lines.get(index), x, y + index * 11, color);
		}
	}

	private static List<Text> getInfoLines(ItemStack stack) {
		List<Text> lines = new ArrayList<>();
		lines.add(Text.literal("貓芽特殊附魔書"));
		lines.add(stack.getName());

		ItemEnchantmentsComponent storedEnchantments = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
		if (storedEnchantments == null) {
			return lines;
		}

		storedEnchantments.getEnchantments().stream()
				.filter(enchantment -> enchantment.getKey()
						.map(key -> key.getValue().getNamespace().equals(CATBUD_ENCHANTMENT_NAMESPACE))
						.orElse(false))
				.forEach(enchantment -> enchantment.getKey().ifPresent(key -> {
					String path = key.getValue().getPath();
					String translationKey = "enchantment.addons." + path;
					int level = storedEnchantments.getLevel(enchantment);
					lines.add(Text.translatable(translationKey).append(Text.literal(" " + level)));

					for (int loreIndex = 0; ; loreIndex++) {
						String loreKey = translationKey + ".lore." + loreIndex;
						if (!I18n.hasTranslation(loreKey)) {
							break;
						}
						lines.add(Text.literal("  ").append(Text.translatable(loreKey)));
					}
				}));

		lines.add(Text.literal("按住 G 顯示，放開關閉"));
		return lines;
	}
}
