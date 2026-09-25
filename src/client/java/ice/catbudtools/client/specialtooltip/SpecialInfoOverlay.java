package ice.catbudtools.client.specialtooltip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import ice.catbudtools.client.config.CatBudConfig;
import ice.catbudtools.client.CatBudToolsClient;
import ice.catbudtools.client.lang.ServerLangRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Keeps track of the special item currently under the cursor and renders its information card.
 */
public final class SpecialInfoOverlay {
	private static final String CATBUD_ENCHANTMENT_NAMESPACE = "addons";
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

		lines.add(Component.empty().append(hoveredStack.getHoverName()).withStyle(hoveredStack.getRarity().color()));

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
					"透過滾輪切換特附頁面顯示(" + (enchantPage + 1) + "/" + totalPage + ")")
					.withStyle(style -> style.withColor(ChatFormatting.GRAY)));
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
			context.text(font, lines.get(index), x, y + index * 11, 0xFFFFFFFF);
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
					int level = enchantments.getLevel(enchantment);
					List<Component> enchantLines = new ArrayList<>();
					MutableComponent title = enchantment.value().description().copy();
					if (title.getStyle().getColor() == null) {
						title.withStyle(ChatFormatting.WHITE);
					}
					title.append(Component.literal(" " + level).withStyle(style -> style.withColor(ChatFormatting.WHITE)));
					enchantLines.add(title);

					// 從伺服器材質包讀取 lore (enchantment.addons.<path>.lore.0, .lore.1, ...)
					List<String> loreLines = collectEnchantLore(path);
					for (String loreLine : loreLines) {
						enchantLines.add(
							Component.literal("  " + loreLine)
							.withStyle(style -> style.withColor(ChatFormatting.GRAY)));
					}
					// conflict 與 maxlevel（只在附魔書上顯示，由伺服器動態提供）
					if (stack.is(Items.ENCHANTED_BOOK) && config.showDetailedEnchantInfo) {
						int maxLevel = enchantment.value().getMaxLevel();
						enchantLines.add(Component.literal("最大等級 " + maxLevel).withStyle(style -> style.withColor(ChatFormatting.GRAY)));
						List<Component> conflicts = getConflicts(enchantment);
						if (!conflicts.isEmpty()) {
							enchantLines.add(Component.literal(""));
							enchantLines.add(Component.literal("與另外" + conflicts.size() + "個衝突").withStyle(style -> style.withColor(ChatFormatting.GRAY)));
							for (Component conflictComp : conflicts) {
								enchantLines.add(
									Component.literal("  ").append(conflictComp));
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

	private static List<Component> getConflicts(Holder<Enchantment> targetEnchant) {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null) {
			return Collections.emptyList();
		}
		return client.level.registryAccess()
				.lookup(Registries.ENCHANTMENT)
				.map(lookup -> lookup.listElements()
						.filter(other -> !other.equals(targetEnchant))
						.filter(other -> !Enchantment.areCompatible(targetEnchant, other))
						.map(other -> {
							MutableComponent desc = other.value().description().copy();
							if (desc.getStyle().getColor() != TextColor.GOLD) {
								desc.withStyle(ChatFormatting.GRAY);
							}
							return (Component) desc;
						})
						.toList())
				.orElse(Collections.emptyList());
	}

	private static List<String> collectEnchantLore(String path) {
		List<String> lore = new ArrayList<>();
		String baseKey = "enchantment.addons." + path + ".lore.";
		for (int i = 0; ; i++) {
			String line = ServerLangRegistry.getRaw(baseKey + i);
			if (line == null) {
				break;
			}
			lore.add(line);
		}
		return lore;
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
		// 擬人化的common tip
		if (id.contains("humanoid_armor_stand_spirit")) {
			itemInfo = SpecialItemRegistry.get("humanoid_armor_stand_spirit_common");
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
