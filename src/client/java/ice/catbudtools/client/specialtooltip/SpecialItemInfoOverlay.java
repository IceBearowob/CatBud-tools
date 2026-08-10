package ice.catbudtools.client.specialtooltip;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import ice.catbudtools.client.config.CatBudConfig;
import ice.catbudtools.client.CatBudToolsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Keeps track of the special item currently under the cursor and renders its information card.
 */
public final class SpecialItemInfoOverlay {
	private static final String CATBUD_ENCHANTMENT_NAMESPACE = "addons";

	private static MutableText catbudTranslate(String key) {return Text.translatable(key);}
	private static ItemStack hoveredStack = ItemStack.EMPTY;
	private static ItemStack lastHoveredStack = ItemStack.EMPTY;
	private static boolean tooltipActive = false;
	private static int enchantPage = 0;
	private static int enchantSectionCount = 0;

	private SpecialItemInfoOverlay() {}
	public static void observe(ItemStack stack) {

		if (stack.isEmpty()) {
			hoveredStack = ItemStack.EMPTY;
			tooltipActive = false;
			return;
		}

		tooltipActive = true;

		if (!ItemStack.areEqual(lastHoveredStack, stack)) {
			enchantPage = 0;
		}

		hoveredStack = stack.copy();
		lastHoveredStack = stack.copy();
	}
	public static void changeEnchantPage(int amount) {
		if (!tooltipActive) {
			return;
		}
		CatBudConfig config = CatBudConfig.getInstance();
		if (config.max_display_enchant == 0){
			return;
		}

		enchantPage += amount;

		if (enchantPage < 0) {
			enchantPage = 0;
		}

	}
	public static void render(DrawContext context, int mouseX, int mouseY) {
		CatBudConfig config = CatBudConfig.getInstance();
		if (!config.ShowTooltip){
			return;
		}
		MinecraftClient client = MinecraftClient.getInstance();
		// 檢查是否為特殊物品
		if (!tooltipActive || hoveredStack.isEmpty() || client.getWindow() == null) {
			return;
		}

		boolean isEnchant =
				SpecialDetector.isSpecialEnchant(hoveredStack)
				|| SpecialDetector.isSpecialAppliedEnchant(hoveredStack);

		boolean isSpecialItem =
				SpecialDetector.hasSpecialItemTooltip(hoveredStack);


		if (!isEnchant && !isSpecialItem) {
			return;
		}
		// 按鍵輸入偵測
		if (!config.AlwaysShowTooltip){
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
		}

		// 決定tooltip的內容
		// Section
		List<TooltipSection> specialSections = new ArrayList<>();
		List<TooltipSection> enchantSections = new ArrayList<>();
		enchantSectionCount = 0;
		
		if (isSpecialItem) {
			specialSections.addAll(
					getSpecialItemInfoSections(hoveredStack)
			);
		}
		if (isEnchant) {
			enchantSections.addAll(
					getEnchantInfoSections(hoveredStack)
			);
			enchantSectionCount = enchantSections.size();
		}
		//處裡section顯示方法
		List<TooltipSection> displaySections = new ArrayList<>();
		displaySections.addAll(specialSections);
		if (config.max_display_enchant == 0){
			displaySections.addAll(enchantSections);
		}else{
			int start = enchantPage * config.max_display_enchant;

			int end = Math.min(
					start + config.max_display_enchant,
					enchantSections.size()
			);

			if (start < end) {
				displaySections.addAll(
					enchantSections.subList(start, end)
				);
			}
		}

		// line
		List<Text> lines = new ArrayList<>();

		lines.add(hoveredStack.getName());

		for (TooltipSection section : displaySections) {
			lines.addAll(section.getLines());
		}
		if (!config.AlwaysShowTooltip){
			lines.add(
				Text.translatable(
							"overlay.catbud-tools.close_hint",
							CatBudToolsClient.OPEN_ITEM_QUERY_KEY.getBoundKeyLocalizedText())
					.styled(style -> style.withColor(Formatting.GRAY)));
		}
		if (enchantSectionCount > config.max_display_enchant && config.max_display_enchant != 0) {
			int totalPage = (enchantSectionCount + config.max_display_enchant - 1) / config.max_display_enchant;
			if (enchantPage >= totalPage) {
    			enchantPage = totalPage - 1;
			}
			lines.add(
				Text.literal(
					"透過滾輪切換特附頁面顯示(" + (enchantPage + 1) + "/" + totalPage + ")"));
		}
		// 依據玩家的 Config 設定動態計算 Tooltip 在螢幕上的 X, Y 繪製座標
		var textRenderer = client.textRenderer;
		int width = lines.stream().mapToInt(textRenderer::getWidth).max().orElse(0) + 12;
		int height = lines.size() * 11 + 8;

		int scaledWidth = client.getWindow().getScaledWidth();
		int scaledHeight = client.getWindow().getScaledHeight();
		int x, y;

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
	private static ItemEnchantmentsComponent getSpecialEnchantments(ItemStack stack) {

		ItemEnchantmentsComponent stored =
				stack.get(DataComponentTypes.STORED_ENCHANTMENTS);

		if (stored != null) {
			return stored;
		}

		return stack.get(DataComponentTypes.ENCHANTMENTS);
	}
	private static List<TooltipSection> getEnchantInfoSections(ItemStack stack) {
		List<TooltipSection> sections = new ArrayList<>();

		ItemEnchantmentsComponent enchantments = getSpecialEnchantments(stack);

		if (enchantments == null) {
			return sections;
		}

		enchantments.getEnchantments().stream()
				.filter(enchantment -> enchantment.getKey()
						.map(key -> key.getValue().getNamespace().equals(CATBUD_ENCHANTMENT_NAMESPACE))
						.orElse(false))
				.forEach(enchantment -> enchantment.getKey().ifPresent(key -> {
					String path = key.getValue().getPath();
					SpecialEnchantInfo enchantInfo = SpecialEnchantRegistry.get(path);
					String enchantName = enchantInfo.getName();
					int level = enchantments.getLevel(enchantment);
					List<Text> enchantLines = new ArrayList<>();
					enchantLines.add(
						catbudTranslate(enchantName).append(Text.literal(" " + level))
							.styled(style -> style.withColor(Formatting.WHITE)));

					// 從 special_enchants.json 讀取 lore 與 conflict
					if (enchantInfo != null) {
						for (String loreLine : enchantInfo.getLore()) {
							enchantLines.add(
								Text.literal("  " + loreLine)
									.styled(style -> style.withColor(Formatting.GRAY)));
						}
						// conflict and maxlevel info（只在附魔書上顯示）
						if (stack.isOf(Items.ENCHANTED_BOOK)) {
							if (!enchantInfo.getConflict().isEmpty()) {
								enchantLines.add(Text.literal("與另外" + enchantInfo.getConflict().size() + "個衝突"));
								for (String conflictLine : enchantInfo.getConflict()) {
									enchantLines.add(
										Text.literal(conflictLine)
											.styled(style -> style.withColor(Formatting.GRAY)));
								}
							}
							enchantLines.add(Text.literal("最大等級" + enchantInfo.getMaxlevel()));
						}
					}
					sections.add(
						new TooltipSection(
							TooltipSection.Type.ENCHANT,
							enchantLines
						)
					);
				}));

		return sections;
	}
	// 特殊物品info
	private static List<TooltipSection> getSpecialItemInfoSections(ItemStack stack) {

		List<TooltipSection> sections = new ArrayList<>();

		List<Text> lines = new ArrayList<>();

		String id = SpecialDetector.getSpecialItemId(stack);
		if (id == null) {
			return sections;
		}
		SpecialItemInfo itemInfo = SpecialItemRegistry.get(id);
		// Lore
		for (String loreLine : itemInfo.getLore()) {
			lines.add(Text.literal(loreLine).styled(style -> style.withColor(Formatting.WHITE)));
		}
		// TIP
		if (!itemInfo.getTip().isEmpty()){
			for (String tipLine : itemInfo.getTip()) {
				lines.add(Text.literal(tipLine).styled(style -> style.withColor(Formatting.GRAY)));
			}
		}
		sections.add(
			new TooltipSection(
				TooltipSection.Type.SPECIAL_ITEM,
				lines
			)
		);

		return sections;
	}
}
