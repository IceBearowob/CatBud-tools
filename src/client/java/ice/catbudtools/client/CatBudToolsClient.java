package ice.catbudtools.client;

import ice.catbudtools.CatBudTools;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class CatBudToolsClient implements ClientModInitializer {
	private static final KeyBinding.Category ITEM_QUERY_CATEGORY = KeyBinding.Category.create(
			CatBudTools.id("item_query")
	);

	private static final KeyBinding OPEN_ITEM_QUERY_KEY = KeyBindingHelper.registerKeyBinding(
			new KeyBinding(
					"key.catbud-tools.open_item_query",
					InputUtil.Type.KEYSYM,
					GLFW.GLFW_KEY_G,
					ITEM_QUERY_CATEGORY
			)
	);

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_ITEM_QUERY_KEY.wasPressed()) {
				if (client.player != null) {
					client.player.sendMessage(Text.literal("貓芽特殊物品查詢：功能準備中"), false);
				}
			}
		});
	}
}