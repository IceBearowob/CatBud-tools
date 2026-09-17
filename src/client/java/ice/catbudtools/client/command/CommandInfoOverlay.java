package ice.catbudtools.client.command;

import java.util.ArrayList;
import java.util.List;

import ice.catbudtools.client.config.CatBudConfig;
import ice.catbudtools.client.lang.ServerLangRegistry;
import ice.catbudtools.client.mixin.CommandSuggestionsAccessor;
import ice.catbudtools.client.mixin.ChatScreenAccessor;
import ice.catbudtools.client.mixin.SuggestionWindowAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public final class CommandInfoOverlay {

    private CommandInfoOverlay() {
    }
    // 一般的指令info
    private static List<Component> regularCommandInfo(String text, CommandInfo info) {
        List<Component> lines = new ArrayList<>();
        // 1. 指令語法 Usage (標題)
        if (info.getUsage() != null && !info.getUsage().isEmpty() && !info.getName().equals("*")) {
            lines.add(Component.literal(info.getUsage()).withStyle(style -> style.withColor(ChatFormatting.YELLOW)));
        }
        if (info.getName().equals("*")){
            lines.add(Component.literal(text).withStyle(style -> style.withColor(ChatFormatting.YELLOW)));
        }
        // 2. 指令說明 Description
        if (info.getDescription() != null && !info.getDescription().isEmpty()) {
            for (String desc : info.getDescription()){
                lines.add(Component.literal(desc).withStyle(style -> style.withColor(ChatFormatting.WHITE)));
            }
        }
        if (info.getTag() != null && !info.getTag().isBlank()){
            if (info.getTag().equals("GUI")) {
                lines.add(Component.translatable("GUI.command.common").withStyle(style -> style.withColor(ChatFormatting.WHITE)));
            }
            if (info.getTag().equals("OP")) {
                lines.add(Component.translatable("OP.command.common").withStyle(style -> style.withColor(ChatFormatting.WHITE)));
            }
        }
        return lines;
    }
    // 給 /special /raffle exchange 專用的info
    private static List<Component> specialCommandInfo(String text, String tag) {
        List<Component> lines = new ArrayList<>();
        String[] special = text.split(" ");
        if (special.length < 3) {
            return lines;
        }

        String rawKey = "";
        if (tag.equals("special_item") || tag.equals("raffle")) {
            rawKey = "items." + special[2];
        } else if (tag.equals("special_entity")) {
            rawKey = "entities." + special[2];
        }

        // usage
        lines.add(Component.literal(text).withStyle(style -> style.withColor(ChatFormatting.YELLOW)));

        // desc
        if (tag.equals("special_item")) {
            // 處理賽季武器
            if (special[2].contains("season")) {
                String[] season = special[2].split("\\.");
                String item = season.length > 2 ? season[2] : "";
                if (item.contains("_0") || item.contains("_1")) {
                    item = item.substring(0, item.indexOf("_", -1));
                }
                String sKey = "seasons.nameless." + item;
                String sName = ServerLangRegistry.getRaw(sKey);
                Component itemComp = sName != null ? Component.literal(sName) : Component.translatable(sKey);
                lines.add(
                    Component.translatable("seasons.common", season.length > 1 ? season[1] : "", itemComp)
                    .withStyle(style -> style.withColor(ChatFormatting.WHITE))
                );
            } else {
                String itemName = ServerLangRegistry.getRaw(rawKey);
                if (itemName == null) {
                    rawKey = special[2].replaceFirst("style", "styles");
                    itemName = ServerLangRegistry.getRaw(rawKey);
                }
                Component itemComp = itemName != null ? Component.literal(itemName) : Component.translatable(rawKey);
                lines.add(
                    Component.translatable("items.common", itemComp)
                    .withStyle(style -> style.withColor(ChatFormatting.WHITE))
                );
            }
        }
        if (tag.equals("special_entity")) {
            String entName = ServerLangRegistry.getRaw(rawKey);
            Component entComp = entName != null ? Component.literal(entName) : Component.translatable(rawKey);
            lines.add(
                Component.translatable("entities.common", entComp)
                .withStyle(style -> style.withColor(ChatFormatting.WHITE))
            );
        }
        if (tag.equals("raffle")) {
            String itemName = ServerLangRegistry.getRaw(rawKey);
            if (itemName == null) {
                itemName = ServerLangRegistry.getRaw("styles." + special[2]);
            }
            Component itemComp = itemName != null ? Component.literal(itemName) : Component.translatable(rawKey);
            lines.add(
                Component.translatable("raffle.common", itemComp)
                .withStyle(style -> style.withColor(ChatFormatting.WHITE))
            );
        }
        return lines;
    }

    // 收集 lore 行 (parentKey.lore.0, parentKey.lore.1, ...)
    private static List<String> collectLore(String parentKey) {
        List<String> lore = new ArrayList<>();
        for (int i = 0; ; i++) {
            String line = ServerLangRegistry.getRaw(parentKey + ".lore." + i);
            if (line == null) break;
            lore.add(line);
        }
        return lore;
    }

    // /buff /config /land config /land license 專用info
    private static List<Component> functionCommandInfo(String text, String tag) {
        List<Component> lines = new ArrayList<>();
        String[] function = text.split(" ");

        if (tag.equals("buff") && function.length > 1) {
            String buffName = function[1];
            String fullKey = ServerLangRegistry.findKeyBySuffix(".assistive." + buffName);
            if (fullKey == null) return lines;
            String title = ServerLangRegistry.getRaw(fullKey);
            if (title == null) return lines;
            // usage
            lines.add(
                Component.literal("/buff " + buffName).append(" [true/def/false]")
                .withStyle(style -> style.withColor(ChatFormatting.YELLOW))
            );
            // desc (中文標題)
            lines.add(Component.translatable("buff.common", Component.literal(title))
                .withStyle(style -> style.withColor(ChatFormatting.WHITE)));
        }
        if (tag.equals("config") && function.length > 1) {
            String configName = function[1];
            String fullKey = ServerLangRegistry.findKeyBySuffix(".config." + configName);
            if (fullKey == null) return lines;
            String title = ServerLangRegistry.getRaw(fullKey);
            if (title == null) return lines;
            // usage
            lines.add(
                Component.literal("/config " + configName).append(" [true/def/false]")
                .withStyle(style -> style.withColor(ChatFormatting.YELLOW))
            );
            // desc (中文標題)
            lines.add(Component.translatable("config.common", Component.literal(title))
                .withStyle(style -> style.withColor(ChatFormatting.WHITE)));
        }
        if (tag.equals("land_config") && function.length > 3) {
            String landconfig = function[3];
            String fullKey = "plugins.territorial_director.config." + landconfig;
            String title = ServerLangRegistry.getRaw(fullKey);
            if (title == null) return lines;
            List<String> lore = collectLore(fullKey);
            // usage
            lines.add(
                Component.literal("/land config " + function[2] + " " + landconfig).append(" [true/def/false]")
                .withStyle(style -> style.withColor(ChatFormatting.YELLOW))
            );
            // desc (中文標題)
            lines.add(Component.translatable("land_config.common", Component.literal(title))
                .withStyle(style -> style.withColor(ChatFormatting.WHITE)));
            // lore
            for (String descLine : lore) {
                lines.add(Component.literal("  " + descLine).withStyle(style -> style.withColor(ChatFormatting.GRAY)));
            }
        }
        if (tag.equals("land_license") && function.length > 4) {
            String landLicenseName = function[4];
            String title;
            List<String> lore;
            if (landLicenseName.equals("@")) {
                title = "所有權限";
                lore = java.util.Collections.emptyList();
            } else {
                String fullKey = "plugins.territorial_director.license." + landLicenseName;
                title = ServerLangRegistry.getRaw(fullKey);
                if (title == null) return lines;
                lore = collectLore(fullKey);
            }
            // usage
            lines.add(
                Component.literal("/land license " + function[2] + " " + function[3] + " " + landLicenseName).append(" [true/def/false]")
                .withStyle(style -> style.withColor(ChatFormatting.YELLOW))
            );
            // desc (中文標題)
            lines.add(Component.translatable("land_license.common", Component.literal(title))
                .withStyle(style -> style.withColor(ChatFormatting.WHITE)));
            // lore
            for (String descLine : lore) {
                lines.add(Component.literal("  " + descLine).withStyle(style -> style.withColor(ChatFormatting.GRAY)));
            }
        } else {
            return lines;
        }

        lines.add(Component.literal("true:開啟;def:預設值;false:關閉").withStyle(style -> style.withColor(ChatFormatting.WHITE)));
        return lines;
    }

    // /room mode 專用info
    private static List<Component> modeCommandInfo(String text) {
        List<Component> lines = new ArrayList<>();
        String[] mode = text.split(" ");
        if (mode.length <= 2) {
            return lines;
        }
        String modeName = mode[2];
        String fullKey = "plugins." + modeName + "." + modeName;
        String title = ServerLangRegistry.getRaw(fullKey);
        if (title == null) {
            return lines;
        }
        // usage
        lines.add(
            Component.literal(text).withStyle(style -> style.withColor(ChatFormatting.YELLOW))
        );
        // desc
        lines.add(
            Component.translatable("mode.common", Component.literal(title))
            .withStyle(style -> style.withColor(ChatFormatting.WHITE))
        );
        return lines;
    }
    // /shop 專用info
    private static List<Component> shopCommandInfo(String text, CommandInfo info) {
        List<Component> lines = new ArrayList<>();
        String[] shop = text.split(" ");
        String key = "items." + shop[1];
        String tag = info.getTag();
        // usage
        if (tag.equals("shop_buy")){
            lines.add(Component.literal("/shop " + shop[1] + " buy <商品數量>").withStyle(style -> style.withColor(ChatFormatting.YELLOW)));
        }
        if (tag.equals("shop_common")){
            lines.add(Component.literal(text).withStyle(style -> style.withColor(ChatFormatting.YELLOW)));
        }
        // desc
        if (info.getDescription() != null && !info.getDescription().isEmpty()) {
            for (String desc : info.getDescription()){
                lines.add(Component.literal(desc).withStyle(style -> style.withColor(ChatFormatting.WHITE)));
            }
        }
        if (tag.equals("shop_buy")){
            lines.add(Component.translatable("shop.common",Component.translatable(key)).withStyle(style -> style.withColor(ChatFormatting.WHITE)));
        }
        return lines;
    }
    // 檢查有沒有特殊info
    private static boolean checkWildCardInfo(CommandInfo info){
        if (info.getUsage() != null && info.getDescription() != null){
            return false;
        }
        if (info.getTag() == null){
            return false;
        }
        if (info.getName().equals("*")){
            return true;
        }
        return false;
    }
    // 特殊tag的info
    private static boolean checkSpecialInfo(CommandInfo info){
        if (info.getTag() == null){
            return false;
        }
        if (info.getTag().contains("shop")){
            return true;
        }
        return false;
    }
	public static void render(GuiGraphicsExtractor context, Screen screen) {
        CatBudConfig config = CatBudConfig.getInstance();
        if (!config.showCommandHelp) {
            return;
        }

        if (!(screen instanceof ChatScreen chatScreen)) {
            return;
        }

        Minecraft client = Minecraft.getInstance();

        EditBox chatField = ((ChatScreenAccessor) chatScreen).catbud$getChatField();
        if (chatField == null) {
            return;
        }

        String text = chatField.getValue();
        if (text == null || text.isBlank() || !text.startsWith("/")) {
            return;
        }

        CatBudCommandRegistry.MatchResult matchResult = CatBudCommandRegistry.findMatch(text);
        if (matchResult == null || matchResult.commandInfo == null) {
            return;
        }

        CommandInfo info = matchResult.commandInfo;

        // 準備要繪製的文字行數
        List<Component> lines = new ArrayList<>();
        String tag = info.getTag();
        if (checkWildCardInfo(info)){
            if (tag.equals("buff") || tag.equals("config") || tag.contains("land")){
                lines.addAll(functionCommandInfo(text, tag));
            }
            if (tag.equals("mode")){
                lines.addAll(modeCommandInfo(text));
            }
            if (tag.contains("special") || tag.equals("raffle")){
                lines.addAll(specialCommandInfo(text, tag));
            }
        }
        if (checkSpecialInfo(info)){
            if (tag.contains("shop")){
                lines.addAll(shopCommandInfo(text, info));
            }
        }
        if (!checkWildCardInfo(info) && !checkSpecialInfo(info)) {
            lines.addAll(regularCommandInfo(text, info));
        }

        if (lines.isEmpty()) {
            return;
        }

        Font font = client.font;
        int maxLineWidth = 0;
        for (Component line : lines) {
            maxLineWidth = Math.max(maxLineWidth, font.width(line));
        }

        int width = maxLineWidth + 12;
        int height = lines.size() * 11 + 6;

        // 計算基準 X 與 Y
        int x = Math.max(4, chatField.getX() + 2);
        int scaledWidth = client.getWindow().getGuiScaledWidth();
        if (x + width > scaledWidth - 4) {
            x = Math.max(4, scaledWidth - width - 4);
        }

        int baseY = chatField.getY() - 6;

        // 檢查原生 CommandSuggestions 補全選單是否存在
        CommandSuggestions suggestor = ((ChatScreenAccessor) chatScreen).catbud$getChatInputSuggestor();
        if (suggestor != null) {
            CommandSuggestions.SuggestionsList window = ((CommandSuggestionsAccessor) suggestor).catbud$getWindow();
            if (window != null) {
                Rect2i area = ((SuggestionWindowAccessor) window).catbud$getArea();
                if (area != null) {
                    int suggestionTopY = area.getY();
                    if (suggestionTopY > 0) {
                        baseY = Math.min(baseY, suggestionTopY - 6);
                    }
                }
            }
        }

        int y = baseY - height;
        if (y < 4) {
            y = 4;
        }

        // 繪製背景與文字
        context.fill(x - 4, y - 4, x + width, y + height, 0xE0101010);
        for (int i = 0; i < lines.size(); i++) {
            context.text(font, lines.get(i), x, y + i * 11, 0xFFFFFFFF);
        }
    }
}
