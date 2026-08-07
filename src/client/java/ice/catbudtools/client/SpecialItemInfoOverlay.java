package ice.catbudtools.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.text.MutableText;

/**
 * Keeps track of the special item currently under the cursor and renders its information card.
 */
public final class SpecialItemInfoOverlay {
	private static final String CATBUD_ENCHANTMENT_NAMESPACE = "addons";
	private static MutableText catbudTranslate(String key) {
		return Text.translatable(key);
	}
	private static boolean hasCatbudInfomation(String key) {
		return I18n.hasTranslation(key);
	}
	private static ItemStack hoveredStack = ItemStack.EMPTY;
	private SpecialItemInfoOverlay() {
	}
	public static void observe(ItemStack stack) {
		hoveredStack = stack.copy();
	}

	public static void render(DrawContext context, int mouseX, int mouseY) {
		MinecraftClient client = MinecraftClient.getInstance();
		// 檢查是否為特殊物品
		if (hoveredStack.isEmpty() || client.getWindow() == null) {
			return;
		}

		boolean isEnchant =
				SpecialItemDetector.isSpecialEnchant(hoveredStack)
				|| SpecialItemDetector.isSpecialAppliedEnchant(hoveredStack);

		boolean isSpecialItem =
				SpecialItemDetector.hasSpecialItemTooltip(hoveredStack);


		if (!isEnchant && !isSpecialItem) {
			return;
		}
		// 按鍵輸入偵測
		InputUtil.Key key = CatBudToolsClient.getOpenItemQueryKey();

		if (key.getCategory() != InputUtil.Type.KEYSYM) {
			return;
		}

		if (GLFW.glfwGetKey(
				client.getWindow().getHandle(),
				key.getCode()
		) != GLFW.GLFW_PRESS) {
			return;
		}
		// 決定tooltip的內容
		List<Text> lines = new ArrayList<>();
		lines.add(hoveredStack.getName());
		if (isEnchant) {
			lines.addAll(
					getEnchantInfoLines(hoveredStack)
			);
		}

		if (isSpecialItem) {
			lines.addAll(
					getSpecialItemInfoLines(hoveredStack)
			);
		}
		lines.add(
			Text.translatable(
						"overlay.catbud-tools.close_hint",
						CatBudToolsClient.OPEN_ITEM_QUERY_KEY.getBoundKeyLocalizedText())
				.styled(style -> style.withColor(Formatting.GRAY)));

		var textRenderer = client.textRenderer;
		int width = lines.stream().mapToInt(textRenderer::getWidth).max().orElse(0) + 12;
		int height = lines.size() * 11 + 8;

		ice.catbudtools.client.config.CatBudConfig config = ice.catbudtools.client.config.CatBudConfig.getInstance();
		int scaledWidth = client.getWindow().getScaledWidth();
		int scaledHeight = client.getWindow().getScaledHeight();
		int x, y;

		// 依據玩家的 Config 設定動態計算 Tooltip 在螢幕上的 X, Y 繪製座標
		switch (config.tooltipPosition) {
			case TOP_LEFT -> {
				x = config.offsetX;
				y = config.offsetY;
			}
			case TOP_RIGHT -> {
				x = scaledWidth - width - config.offsetX;
				y = config.offsetY;
			}
			case BOTTOM_LEFT -> {
				x = config.offsetX;
				y = scaledHeight - height - config.offsetY;
			}
			case BOTTOM_RIGHT -> {
				x = scaledWidth - width - config.offsetX;
				y = scaledHeight - height - config.offsetY;
			}
			case CENTER -> {
				x = (scaledWidth - width) / 2 + config.offsetX;
				y = (scaledHeight - height) / 2 + config.offsetY;
			}
			case FOLLOW_MOUSE -> {
				x = mouseX + config.offsetX;
				y = mouseY + config.offsetY;
			}
			default -> {
				x = mouseX + 14;
				y = mouseY + 14;
			}
		}

		context.fill(x - 4, y - 4, x + width, y + height, 0xE0101010);
		for (int index = 0; index < lines.size(); index++) {
			int color = index == 0 ? 0xFFFFD85A : (index == 1 ? 0xFFFFFFFF : 0xFFB8B8B8);
			context.drawTextWithShadow(textRenderer, lines.get(index), x, y + index * 11, color);
		}
	}
	// 特附info
	private static List<Text> getEnchantInfoLines(ItemStack stack) {
		List<Text> lines = new ArrayList<>();

		ItemEnchantmentsComponent enchantments = getSpecialEnchantments(stack);

		if (enchantments == null) {
			return lines;
		}

		enchantments.getEnchantments().stream()
				.filter(enchantment -> enchantment.getKey()
						.map(key -> key.getValue().getNamespace().equals(CATBUD_ENCHANTMENT_NAMESPACE))
						.orElse(false))
				.forEach(enchantment -> enchantment.getKey().ifPresent(key -> {
					String path = key.getValue().getPath();
					String translationKey = "enchantment.addons." + path;
					int level = enchantments.getLevel(enchantment);
					lines.add(
						catbudTranslate(translationKey).append(Text.literal(" " + level))
							.styled(style -> style.withColor(Formatting.WHITE)));
							

					for (int loreIndex = 0; ; loreIndex++) {
						String loreKey = translationKey + ".lore." + loreIndex;
						if (!hasCatbudInfomation(loreKey)) {
							break;
						}
						lines.add(
							Text.literal("  ").append(catbudTranslate(loreKey))
								.styled(style -> style.withColor(Formatting.GRAY)));
					}
				}));

		return lines;
	}
	private static ItemEnchantmentsComponent getSpecialEnchantments(ItemStack stack) {

		ItemEnchantmentsComponent stored =
				stack.get(DataComponentTypes.STORED_ENCHANTMENTS);

		if (stored != null) {
			return stored;
		}


		return stack.get(DataComponentTypes.ENCHANTMENTS);
	}
	// 特殊物品info
	private static List<Text> getSpecialItemInfoLines(ItemStack stack) {

		List<Text> lines = new ArrayList<>();

		String id = SpecialItemDetector.getSpecialItemId(stack);

		if (id == null) {
			return lines;
		}
		// Lore
		for (int i = 0; ; i++) {

			String loreKey = id + ".lore." + i;
			if (!hasCatbudInfomation(loreKey)) {
				break;
			}
			lines.add(catbudTranslate(loreKey).styled(style -> style.withColor(Formatting.WHITE)));
		}
		// TIP
		String tip;
		for (int i = 0 ; ; i++){
			if (stack.getName().getString().contains("擬人化盔甲座靈魂")){
				tip = "humanoid_armor_stand_spirit.tip." + i;
			}else{
				tip = id + ".tip." + i;
			}
			if (!hasCatbudInfomation(tip)) {
				break;
			}
			lines.add(catbudTranslate(tip).styled(style -> style.withColor(Formatting.GRAY)));
		}

		return lines;
	}
}
