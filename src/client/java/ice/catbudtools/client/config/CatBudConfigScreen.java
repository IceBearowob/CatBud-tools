package ice.catbudtools.client.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import ice.catbudtools.client.config.widget.OffsetSlider;

/**
 * 遊戲內 GUI 設定選單畫面，允許玩家調整 Tooltip 位置與 X/Y 軸偏移量。
 */
public class CatBudConfigScreen extends Screen {
	private final Screen parent;
	private TooltipPosition position;
	private int offsetX;
	private int offsetY;
	
	private OffsetSlider xSlider;
	private OffsetSlider ySlider;
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
		this.xSlider = this.addDrawableChild(
			new OffsetSlider(
					centerX - 100,
					startY + 28,
					160,
					20,
					this.offsetX,
					"X Offset",
					value -> this.offsetX = value
			)
		);
		this.addDrawableChild(
			ButtonWidget.builder(
					Text.literal("↺"),
					button -> {

						this.offsetX = 14;
						this.xSlider.resetTo(14);

					}
			)
			.dimensions(
					centerX + 65,
					startY + 28,
					35,
					20
			)
			.build()
		);
		// Y 軸偏移量按鈕
		this.ySlider = this.addDrawableChild(
			new OffsetSlider(
					centerX - 100,
					startY + 56,
					160,
					20,
					this.offsetY,
					"Y Offset",
					value -> this.offsetY = value
			)
		);
		this.addDrawableChild(
			ButtonWidget.builder(
					Text.literal("↺"),
					button -> {

						this.offsetY = 14;
						this.ySlider.resetTo(14);

					}
			)
			.dimensions(
					centerX + 65,
					startY + 56,
					35,
					20
			)
			.build()
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
