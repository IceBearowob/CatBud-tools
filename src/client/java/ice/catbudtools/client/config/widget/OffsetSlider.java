package ice.catbudtools.client.config.widget;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.IntConsumer;


public class OffsetSlider extends SliderWidget {

	private final String name;
	private final IntConsumer valueConsumer;


	public OffsetSlider(
			int x,
			int y,
			int width,
			int height,
			int value,
			String name,
			IntConsumer valueConsumer
	) {

		super(
				x,
				y,
				width,
				height,
				Text.literal(name + ": " + value),
				(value + 200) / 400.0
		);

		this.name = name;
		this.valueConsumer = valueConsumer;

		updateMessage();
	}


	private int getIntValue() {

		return (int)Math.round(
				-200 + this.value * 400
		);
	}


	@Override
	protected void updateMessage() {

		setMessage(
				Text.literal(
						name + ": " + getIntValue()
				)
		);
	}


	@Override
	protected void applyValue() {

		valueConsumer.accept(
				getIntValue()
		);
	}


	/**
	 * 給外部同步 Slider 使用
	 */
	public void setIntValue(int value) {

		this.setValue(
				(value + 200) / 400.0
		);

		updateMessage();
	}
    public void resetTo(int value) {

        this.setValue(
                (value + 200) / 400.0
        );

        updateMessage();
    }
}