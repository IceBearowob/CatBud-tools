package ice.catbudtools.client.config;

import net.minecraft.network.chat.Component;

/**
 * 定義 Tooltip 資訊框在螢幕上的顯示位置模式。
 */
public enum TooltipPosition {
	FOLLOW_MOUSE("catbud-tools.config.position.follow_mouse"),
	TOP_LEFT("catbud-tools.config.position.top_left"),
	TOP_RIGHT("catbud-tools.config.position.top_right"),
	BOTTOM_LEFT("catbud-tools.config.position.bottom_left"),
	BOTTOM_RIGHT("catbud-tools.config.position.bottom_right"),
	CENTER("catbud-tools.config.position.center");

	private final String translationKey;

	TooltipPosition(String translationKey) {
		this.translationKey = translationKey;
	}

	public Component getText() {
		return Component.translatable(translationKey);
	}
}
