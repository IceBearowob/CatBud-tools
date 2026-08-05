package ice.catbudtools.client.config.yacl;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import ice.catbudtools.client.config.CatBudConfig;
import ice.catbudtools.client.config.TooltipPosition;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.OptionDescription;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class YaclConfigScreen {


    public static Screen create(Screen parent) {

        CatBudConfig config = CatBudConfig.getInstance();


        Option<Integer> xOffset =
                Option.<Integer>createBuilder()
                .name(Text.literal("X偏移"))
                .description(OptionDescription.of(Text.literal("調整Tooltip的X方向偏移量")))
                .binding(
                        CatBudConfig.DEFAULT_OFFSET_X,
                        () -> config.offsetX,
                        value -> config.offsetX = value
                )
                .controller(
                        opt ->
                                IntegerSliderControllerBuilder
                                .create(opt)
                                .range(-200, 200)
                                .step(1)
                )
                .build();
        Option<Integer> yOffset =
                Option.<Integer>createBuilder()
                .name(Text.literal("Y偏移"))
                .description(OptionDescription.of(Text.literal("調整Tooltip的Y方向偏移量")))
                .binding(
                        CatBudConfig.DEFAULT_OFFSET_Y,
                        () -> config.offsetY,
                        value -> config.offsetY = value
                )
                .controller(
                        opt ->
                                IntegerSliderControllerBuilder
                                .create(opt)
                                .range(-200, 200)
                                .step(1)
                )
                .build();
        Option<TooltipPosition> tooltipPosition =
                Option.<TooltipPosition>createBuilder()
                .name(Text.literal("Tooltip位置"))
                .description(OptionDescription.of(Text.literal("調整Tooltip的顯示位置")))
                .binding(
                    CatBudConfig.DEFAULT_TOOLTIP_POSITION,
                    () -> config.tooltipPosition,
                    value -> config.tooltipPosition = value
                )
                .controller(
                        opt ->
                                EnumControllerBuilder
                                .create(opt)
                                .enumClass(TooltipPosition.class)
                                .formatValue(value -> value.getText())

                )
                .build();

        ButtonOption resetButton =
                ButtonOption.createBuilder()
                .name(Text.literal("全部重置"))
                .text(Text.literal("點我重置"))
                .description(OptionDescription.of(Text.literal("就是重置，你還想知道啥?")))
                .action((screen, button) -> {
                    config.reset();

                    xOffset.requestSet(CatBudConfig.DEFAULT_OFFSET_X);
                    yOffset.requestSet(CatBudConfig.DEFAULT_OFFSET_Y);
                    tooltipPosition.requestSet(CatBudConfig.DEFAULT_TOOLTIP_POSITION);
                    CatBudConfig.save();
                })
                .build();

        return YetAnotherConfigLib.createBuilder().title(Text.literal("CatBud Tools"))
                .category(ConfigCategory.createBuilder().name(Text.literal("Tooltip"))
                    .option(xOffset)
                    .option(yOffset)
                    .option(tooltipPosition)
                    .option(resetButton)
                    .build()
                )
                .save(() -> CatBudConfig.save())
                .build()
                .generateScreen(parent);
    }
}