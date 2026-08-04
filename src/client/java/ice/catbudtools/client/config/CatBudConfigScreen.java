package ice.catbudtools.client.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

/**
 * 遊戲內 GUI 設定選單畫面，允許玩家調整 Tooltip 位置與 X/Y 軸偏移量。
 */
public class CatBudConfigScreen extends Screen {
	private final Screen parent;
	private TooltipPosition position;
	private int offsetX;
	private int offsetY;

	public CatBudConfigScreen(Screen parent) {
		super(Text.literal("CatBud Tools 設定"));
		this.parent = parent;
		CatBudConfig config = CatBudConfig.getInstance();
		this.position = config.tooltipPosition;
		this.offsetX = config.offsetX;
		this.offsetY = config.offsetY;
	}

	@Override
	protected void init() {
		super.init();
		int centerX = this.width / 2;
		int startY = this.height / 4;

		// Tooltip 位置切換按鈕
		this.addDrawableChild(
			CyclingButtonWidget.builder(TooltipPosition::getText, this.position)
				.values(TooltipPosition.values())
				.build(
					centerX - 100, startY, 200, 20,
					Text.literal("Tooltip 位置"),
					(button, value) -> this.position = value
				)
		);

		// X 軸偏移量按鈕
		this.addDrawableChild(
			ButtonWidget.builder(
				Text.literal("X 軸偏移: " + this.offsetX),
				button -> {
					this.offsetX += 5;
					if (this.offsetX > 200) this.offsetX = -200;
					button.setMessage(Text.literal("X 軸偏移: " + this.offsetX));
				}
			).dimensions(centerX - 100, startY + 28, 200, 20).build()
		);

		// Y 軸偏移量按鈕
		this.addDrawableChild(
			ButtonWidget.builder(
				Text.literal("Y 軸偏移: " + this.offsetY),
				button -> {
					this.offsetY += 5;
					if (this.offsetY > 200) this.offsetY = -200;
					button.setMessage(Text.literal("Y 軸偏移: " + this.offsetY));
				}
			).dimensions(centerX - 100, startY + 56, 200, 20).build()
		);

		// 恢復預設值按鈕
		this.addDrawableChild(
			ButtonWidget.builder(
				Text.literal("恢復預設值"),
				button -> {
					this.position = TooltipPosition.FOLLOW_MOUSE;
					this.offsetX = 14;
					this.offsetY = 14;
					this.clearAndInit();
				}
			).dimensions(centerX - 100, startY + 90, 200, 20).build()
		);

		// 完成/儲存按鈕
		this.addDrawableChild(
			ButtonWidget.builder(
				ScreenTexts.DONE,
				button -> {
					CatBudConfig config = CatBudConfig.getInstance();
					config.tooltipPosition = this.position;
					config.offsetX = this.offsetX;
					config.offsetY = this.offsetY;
					CatBudConfig.save();
					if (this.client != null) {
						this.client.setScreen(this.parent);
					}
				}
			).dimensions(centerX - 100, startY + 120, 200, 20).build()
		);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
	}

	@Override
	public void close() {
		if (this.client != null) {
			this.client.setScreen(this.parent);
		}
	}
}
