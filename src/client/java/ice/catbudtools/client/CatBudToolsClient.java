package ice.catbudtools.client;

import ice.catbudtools.CatBudTools;
import ice.catbudtools.client.mixin.KeyBindingAccessor;
import ice.catbudtools.client.config.CatBudConfig;
import ice.catbudtools.client.specialtooltip.SpecialEnchantRegistry;
import ice.catbudtools.client.specialtooltip.SpecialInfoOverlay;
import ice.catbudtools.client.specialtooltip.SpecialItemRegistry;
import ice.catbudtools.client.specialtooltip.SpecialDetector;
import ice.catbudtools.client.command.CommandInfoOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;


public class CatBudToolsClient implements ClientModInitializer {

    // 取得當前伺服器 IP / 網址
    private static String getCurrentServerAddress() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isInSingleplayer()) {
            return "singleplayer";
        }
        ServerInfo serverInfo = client.getCurrentServerEntry();
        return (serverInfo != null && serverInfo.address != null) ? serverInfo.address : "";
    }
    // 判斷是否為目標伺服器（建議轉成小寫比對，避免大小寫問題）
    public static boolean isCatBudServer() {
        String address = getCurrentServerAddress().toLowerCase();
        return address.contains("catbud.net");
    }
    
	private static final KeyBinding.Category ITEM_QUERY_CATEGORY =
			KeyBinding.Category.create(
					CatBudTools.id("item_query")
			);


	public static final KeyBinding OPEN_ITEM_QUERY_KEY =
			KeyBindingHelper.registerKeyBinding(
					new KeyBinding(
							"key.catbud-tools.open_item_query",
							InputUtil.Type.KEYSYM,
							GLFW.GLFW_KEY_G,
							ITEM_QUERY_CATEGORY
					)
			);
	public static InputUtil.Key getOpenItemQueryKey() {
		return ((KeyBindingAccessor) OPEN_ITEM_QUERY_KEY).catbud$getBoundKey();
	}
	@Override
	public void onInitializeClient() {
		CatBudConfig.load();
		ice.catbudtools.client.command.CatBudCommandRegistry.load();
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
							Text.translatable(
									"tooltip.catbud-tools.special_item_hint",
									OPEN_ITEM_QUERY_KEY.getBoundKeyLocalizedText()
							)
						);
					}
				}
			}
		);
		// tooltip & command info overlay
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			ScreenEvents.beforeRender(screen).register((currentScreen, drawContext, mouseX, mouseY, tickDelta) -> {
				SpecialInfoOverlay.observe(ItemStack.EMPTY);
			});

			ScreenEvents.afterRender(screen).register((currentScreen, drawContext, mouseX, mouseY, tickDelta) -> {
					if (!isCatBudServer()) {
						return;
					}
					SpecialInfoOverlay.render(
							drawContext,
							mouseX,
							mouseY
					);

					CommandInfoOverlay.render(
							drawContext,
							currentScreen
					);

				}
			);
		});
	}
}