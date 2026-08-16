package ice.catbudtools.client;

import ice.catbudtools.client.mixin.KeyMappingAccessor;
import ice.catbudtools.client.config.CatBudConfig;
import ice.catbudtools.client.specialtooltip.SpecialEnchantRegistry;
import ice.catbudtools.client.specialtooltip.SpecialInfoOverlay;
import ice.catbudtools.client.specialtooltip.SpecialItemRegistry;
import ice.catbudtools.client.specialtooltip.SpecialDetector;
import ice.catbudtools.client.command.CatBudCommandRegistry;
import ice.catbudtools.client.command.CommandInfoOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class CatBudToolsClient implements ClientModInitializer {

    // 取得當前伺服器 IP / 網址
    private static String getCurrentServerAddress() {
        Minecraft client = Minecraft.getInstance();
        if (client.hasSingleplayerServer()) {
            return "singleplayer";
        }
        ServerData serverInfo = client.getCurrentServer();
        return (serverInfo != null && serverInfo.ip != null) ? serverInfo.ip : "";
    }

    // 判斷是否為目標伺服器（建議轉成小寫比對，避免大小寫問題）
    public static boolean isCatBudServer() {
        String address = getCurrentServerAddress().toLowerCase();
        return address.contains("catbud.net");
    }

    private static final KeyMapping.Category ITEM_QUERY_CATEGORY =
            KeyMapping.Category.register(
                    Identifier.fromNamespaceAndPath("catbud-tools", "item_query")
            );

	public static final KeyMapping OPEN_ITEM_QUERY_KEY =
			KeyMappingHelper.registerKeyMapping(
					new KeyMapping(
							"key.catbud-tools.open_item_query",
							InputConstants.Type.KEYSYM,
							GLFW.GLFW_KEY_G,
							ITEM_QUERY_CATEGORY
					)
			);

	public static InputConstants.Key getOpenItemQueryKey() {
		return ((KeyMappingAccessor) OPEN_ITEM_QUERY_KEY).catbud$getBoundKey();
	}

	@Override
	public void onInitializeClient() {
		CatBudConfig.load();
		CatBudCommandRegistry.load();
		SpecialEnchantRegistry.load();
		SpecialItemRegistry.load();
		// Tooltip 偵測特殊物品
		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
				CatBudConfig config = CatBudConfig.getInstance();
				if (!config.ShowTooltip){
					return;
				}
				if (!isCatBudServer()) {
					return;
				}
				if (SpecialDetector.isSpecialEnchant(stack)
						|| SpecialDetector.isSpecialAppliedEnchant(stack)
						|| SpecialDetector.hasSpecialItemTooltip(stack)) {

					SpecialInfoOverlay.observe(stack);
					
					if (!config.AlwaysShowTooltip){
						lines.add(
							Component.translatable(
									"tooltip.catbud-tools.special_item_hint",
									OPEN_ITEM_QUERY_KEY.getTranslatedKeyMessage()
							)
						);
					}
				}
			}
		);

		// tooltip & command info overlay
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			ScreenEvents.afterForeground(screen).register((currentScreen, guiGraphics, mouseX, mouseY, tickDelta) -> {
					if (!isCatBudServer()) {
						return;
					}
					SpecialInfoOverlay.render(
							guiGraphics,
							mouseX,
							mouseY
					);

					CommandInfoOverlay.render(
							guiGraphics,
							currentScreen
						);

					SpecialInfoOverlay.observe(ItemStack.EMPTY);

				}
			);
		});
	}
}
