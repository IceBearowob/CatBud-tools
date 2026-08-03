package ice.catbudtools.client;

import ice.catbudtools.CatBudTools;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class CatBudToolsClient implements ClientModInitializer {
	private static final KeyBinding.Category ITEM_QUERY_CATEGORY = KeyBinding.Category.create(
			CatBudTools.id("item_query")
	);

	public static final KeyBinding OPEN_ITEM_QUERY_KEY = KeyBindingHelper.registerKeyBinding(
			new KeyBinding(
					"key.catbud-tools.open_item_query",
					InputUtil.Type.KEYSYM,
					GLFW.GLFW_KEY_G,
					ITEM_QUERY_CATEGORY
			)
	);

	@Override
	public void onInitializeClient() {
		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			if (SpecialItemDetector.isSpecial(stack)) {
				SpecialItemInfoOverlay.observe(stack);
				lines.add(Text.translatable("tooltip.catbud-tools.special_item_hint"));
			}
		});

		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			ScreenEvents.beforeRender(screen).register((currentScreen, drawContext, mouseX, mouseY, tickDelta) ->
					SpecialItemInfoOverlay.beginFrame()
			);
			ScreenEvents.afterRender(screen).register((currentScreen, drawContext, mouseX, mouseY, tickDelta) ->
					SpecialItemInfoOverlay.render(drawContext, mouseX, mouseY)
			);
		});
	}
}
