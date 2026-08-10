package ice.catbudtools.client.config.yacl;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import ice.catbudtools.client.config.CatBudConfig;
import ice.catbudtools.client.config.TooltipPosition;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class YaclConfigScreen {


    public static Screen create(Screen parent) {

        CatBudConfig config = CatBudConfig.getInstance();
        Option<Boolean> ShowTooltip = 
                Option.<Boolean>createBuilder()
                .name(Text.literal("顯示Tooltip"))
                .description(OptionDescription.of(Text.literal("開啟:顯示Tooltip\n關閉:不顯示Tooltip")))
                .binding(
                        CatBudConfig.DEFAULT_SHOWTOOLTIP,
                        () -> config.ShowTooltip,
                        value -> config.ShowTooltip = value
                )
                .controller(BooleanControllerBuilder::create)
                .build();
        Option<Boolean> AlwaysShowTooltip = 
                Option.<Boolean>createBuilder()
                .name(Text.literal("始終顯示Tooltip"))
                .description(OptionDescription.of(Text.literal("開啟:不需要按按鍵就會顯示Tooltip\n關閉:需要按按鍵才會顯示Tooltip")))
                .binding(
                        CatBudConfig.DEFAULT_ALWAYSTOOLTIP,
                        () -> config.AlwaysShowTooltip,
                        value -> config.AlwaysShowTooltip = value
                )
                .controller(BooleanControllerBuilder::create)
                .build();
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
        Option<Integer> max_display_enchant =
                Option.<Integer>createBuilder()
                .name(Text.literal("最大特附顯示數量"))
                .description(OptionDescription.of(Text.literal("如果說特附多到你沒辦法完整看到所有內容，就自行調整數值\n調成0代表完全顯示")))
                .binding(
                        CatBudConfig.DEFAULT_MAX_DISPLAY_ENCHANT,
                        () -> config.max_display_enchant,
                        value -> config.max_display_enchant = value
                )
                .controller(
                        opt ->
                                IntegerSliderControllerBuilder
                                .create(opt)
                                .range(0, 30)
                                .step(1)
                )
                .build();
        Option<Boolean> showCommandHelp = 
                Option.<Boolean>createBuilder()
                .name(Text.literal("顯示貓芽指令說明"))
                .description(OptionDescription.of(Text.literal("開啟:輸入貓芽指令時於聊天框上方顯示語法與說明\n關閉:不顯示指令說明")))
                .binding(
                        CatBudConfig.DEFAULT_SHOW_COMMAND_HELP,
                        () -> config.showCommandHelp,
                        value -> config.showCommandHelp = value
                )
                .controller(BooleanControllerBuilder::create)
                .build();
        ButtonOption resetButton =
                ButtonOption.createBuilder()
                .name(Text.literal("全部重置"))
                .text(Text.literal("點我重置"))
                .description(OptionDescription.of(Text.literal("就是重置，你還想知道啥?")))
                .action((screen, button) -> {
                        config.reset();

                        ShowTooltip.requestSet(CatBudConfig.DEFAULT_SHOWTOOLTIP);
                        AlwaysShowTooltip.requestSet(CatBudConfig.DEFAULT_ALWAYSTOOLTIP);
                        showCommandHelp.requestSet(CatBudConfig.DEFAULT_SHOW_COMMAND_HELP);
                        xOffset.requestSet(CatBudConfig.DEFAULT_OFFSET_X);
                        yOffset.requestSet(CatBudConfig.DEFAULT_OFFSET_Y);
                        tooltipPosition.requestSet(CatBudConfig.DEFAULT_TOOLTIP_POSITION);
                        max_display_enchant.requestSet(CatBudConfig.DEFAULT_MAX_DISPLAY_ENCHANT);
                        CatBudConfig.save();
                })
                .build();

        return YetAnotherConfigLib.createBuilder().title(Text.literal("CatBud Tools"))
                .category(ConfigCategory.createBuilder().name(Text.literal("Tooltip"))
                        .option(ShowTooltip)
                        .option(AlwaysShowTooltip)
                        .option(showCommandHelp)
                        .option(xOffset)
                        .option(yOffset)
                        .option(tooltipPosition)
                        .option(max_display_enchant)
                        .option(resetButton)
                        .build()
                )
                .save(() -> CatBudConfig.save())
                .build()
                .generateScreen(parent);
    }
}