package ice.catbudtools.client;

import ice.catbudtools.CatBudTools;
import ice.catbudtools.client.mixin.KeyBindingAccessor;
import ice.catbudtools.client.config.CatBudConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;


public class CatBudToolsClient implements ClientModInitializer {

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
		// Tooltip 偵測特殊物品
		ItemTooltipCallback.EVENT.register(
				(stack, context, type, lines) -> {
					CatBudConfig config = CatBudConfig.getInstance();
					if (!config.ShowTooltip){
						return;
					}
					if (SpecialItemDetector.isSpecialEnchant(stack)
							|| SpecialItemDetector.isSpecialAppliedEnchant(stack)
							|| SpecialItemDetector.hasSpecialItemTooltip(stack)) {

						SpecialItemInfoOverlay.observe(stack);
						
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
				SpecialItemInfoOverlay.observe(ItemStack.EMPTY);
			});

			ScreenEvents.afterRender(screen).register(
					(currentScreen, drawContext, mouseX, mouseY, tickDelta) -> {

						SpecialItemInfoOverlay.render(
								drawContext,
								mouseX,
								mouseY
						);

						ice.catbudtools.client.command.CommandInfoOverlay.render(
								drawContext,
								currentScreen
						);

					}
			);
		});
	}
}