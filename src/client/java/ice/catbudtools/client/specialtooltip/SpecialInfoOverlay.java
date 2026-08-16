package ice.catbudtools.client.specialtooltip;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import ice.catbudtools.client.config.CatBudConfig;
import ice.catbudtools.client.CatBudToolsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Keeps track of the special item currently under the cursor and renders its information card.
 */
public final class SpecialInfoOverlay {
	private static final String CATBUD_ENCHANTMENT_NAMESPACE = "addons";

	private static MutableComponent catbudTranslate(String key) {return Component.translatable(key);}
	private static ItemStack hoveredStack = ItemStack.EMPTY;
	private static ItemStack lastHoveredStack = ItemStack.EMPTY;
	private static boolean tooltipActive = false;
	private static int enchantPage = 0;
	private static int enchantSectionCount = 0;

	private SpecialInfoOverlay() {}
	public static void observe(ItemStack stack) {

		if (stack.isEmpty()) {
			hoveredStack = ItemStack.EMPTY;
			tooltipActive = false;
			return;
		}

		tooltipActive = true;

		if (!ItemStack.matches(lastHoveredStack, stack)) {
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
	public static void render(GuiGraphicsExtractor context, int mouseX, int mouseY) {
		CatBudConfig config = CatBudConfig.getInstance();
		if (!config.ShowTooltip){
			return;
		}
		Minecraft client = Minecraft.getInstance();
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
			InputConstants.Key key = CatBudToolsClient.getOpenItemQueryKey();

			if (key.getType() != InputConstants.Type.KEYSYM) {
				return;
			}

			if (GLFW.glfwGetKey(client.getWindow().handle(), key.getValue()) != GLFW.GLFW_PRESS) {
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
		List<Component> lines = new ArrayList<>();

		lines.add(hoveredStack.getHoverName());

		for (TooltipSection section : displaySections) {
			lines.addAll(section.getLines());
		}
		if (!config.AlwaysShowTooltip){
			lines.add(
				Component.translatable(
							"overlay.catbud-tools.close_hint",
							CatBudToolsClient.OPEN_ITEM_QUERY_KEY.getTranslatedKeyMessage())
					.withStyle(style -> style.withColor(ChatFormatting.GRAY)));
		}
		if (enchantSectionCount > config.max_display_enchant && config.max_display_enchant != 0) {
			int totalPage = (enchantSectionCount + config.max_display_enchant - 1) / config.max_display_enchant;
			if (enchantPage >= totalPage) {
    			enchantPage = totalPage - 1;
			}
			lines.add(
				Component.literal(
					"透過滾輪切換特附頁面顯示(" + (enchantPage + 1) + "/" + totalPage + ")"));
		}
		// 依據玩家的 Config 設定動態計算 Tooltip 在螢幕上的 X, Y 繪製座標
		Font font = client.font;
		int width = lines.stream().mapToInt(font::width).max().orElse(0) + 12;
		int height = lines.size() * 11 + 8;

		int scaledWidth = client.getWindow().getGuiScaledWidth();
		int scaledHeight = client.getWindow().getGuiScaledHeight();
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
			context.text(font, lines.get(index), x, y + index * 11, color);
		}
	}
	// 特附info
	private static ItemEnchantments getSpecialEnchantments(ItemStack stack) {

		ItemEnchantments stored =
				stack.get(DataComponents.STORED_ENCHANTMENTS);

		if (stored != null) {
			return stored;
		}

		return stack.get(DataComponents.ENCHANTMENTS);
	}
	private static List<TooltipSection> getEnchantInfoSections(ItemStack stack) {
		List<TooltipSection> sections = new ArrayList<>();
		CatBudConfig config = CatBudConfig.getInstance();
		ItemEnchantments enchantments = getSpecialEnchantments(stack);

		if (enchantments == null) {
			return sections;
		}

		enchantments.keySet().stream()
				.filter(enchantment -> enchantment.unwrapKey()
						.map(key -> key.identifier().getNamespace().equals(CATBUD_ENCHANTMENT_NAMESPACE))
						.orElse(false))
				.forEach(enchantment -> enchantment.unwrapKey().ifPresent(key -> {
					String path = key.identifier().getPath();
					SpecialEnchantInfo enchantInfo = SpecialEnchantRegistry.get(path);
					String enchantName = enchantInfo != null ? enchantInfo.getName() : path;
					int level = enchantments.getLevel(enchantment);
					List<Component> enchantLines = new ArrayList<>();
					enchantLines.add(
						catbudTranslate(enchantName).append(Component.literal(" " + level))
							.withStyle(style -> style.withColor(ChatFormatting.WHITE)));

					// 從 special_enchants.json 讀取 lore 與 conflict
					if (enchantInfo != null) {
						for (String loreLine : enchantInfo.getLore()) {
							enchantLines.add(
								Component.literal("  " + loreLine)
								.withStyle(style -> style.withColor(ChatFormatting.GRAY)));
						}
						// conflict(只在附魔書上顯示）
						if (stack.is(Items.ENCHANTED_BOOK) && config.showDetailedEnchantInfo) {
							enchantLines.add(Component.literal("最大等級 " + enchantInfo.getMaxlevel()).withStyle(style -> style.withColor(ChatFormatting.GRAY)));
							if (!enchantInfo.getConflict().isEmpty()) {
								enchantLines.add(Component.literal(""));
								enchantLines.add(Component.literal("與另外" + enchantInfo.getConflict().size() + "個衝突"));
								for (String conflictLine : enchantInfo.getConflict()) {
									enchantLines.add(
										Component.literal("  " + conflictLine)
										.withStyle(style -> style.withColor(ChatFormatting.WHITE)));
								}
							}
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

		List<Component> lines = new ArrayList<>();

		String id = SpecialDetector.getSpecialItemId(stack);
		if (id == null) {
			return sections;
		}
		SpecialItemInfo itemInfo = SpecialItemRegistry.get(id);
		// Lore
		for (String loreLine : itemInfo.getLore()) {
			lines.add(Component.literal(loreLine).withStyle(style -> style.withColor(ChatFormatting.WHITE)));
		}
		// TIP
		if (!itemInfo.getTip().isEmpty()){
			for (String tipLine : itemInfo.getTip()) {
				lines.add(Component.literal(tipLine).withStyle(style -> style.withColor(ChatFormatting.GRAY)));
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
