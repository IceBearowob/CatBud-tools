package ice.catbudtools.client.command;

import java.util.ArrayList;
import java.util.List;

import ice.catbudtools.client.config.CatBudConfig;
import ice.catbudtools.client.mixin.ChatInputSuggestorAccessor;
import ice.catbudtools.client.mixin.ChatScreenAccessor;
import ice.catbudtools.client.mixin.SuggestionWindowAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
public final class CommandInfoOverlay {

    private CommandInfoOverlay() {
    }
    // 一般的指令info
    private static List<Text> regularCommandInfo(CommandInfo info) {
        List<Text> lines = new ArrayList<>();
        // 1. 指令語法 Usage (標題)
        if (info.getUsage() != null && !info.getUsage().isEmpty()) {
            lines.add(Text.literal(info.getUsage()).styled(style -> style.withColor(Formatting.YELLOW).withBold(true)));
        }

        // 2. 指令說明 Description
        if (info.getTag() != null && !info.getTag().isBlank()){
            if (info.getTag().equals("OP")) {
                lines.add(Text.translatable("OP.command.common").styled(style -> style.withColor(Formatting.WHITE)));
            }
        }
        if (info.getDescription() != null && !info.getDescription().isEmpty()) {
            for (String desc : info.getDescription()){
                lines.add(Text.literal(desc).styled(style -> style.withColor(Formatting.WHITE)));
            }
        }
        if (info.getTag() != null && !info.getTag().isBlank()){
            if (info.getTag().equals("GUI")) {
                lines.add(Text.translatable("GUI.command.common").styled(style -> style.withColor(Formatting.WHITE)));
            }
        }
        return lines;
    }
    // 給 /special 專用的info
    private static List<Text> specialCommandInfo(String text, String tag) {
        List<Text> lines = new ArrayList<>();
        String[] special = text.split(" ");
        String key = "";
        if (tag.equals("special_item")){
            key = "items." + special[2];
        }
        else if (tag.equals("special_entity")){
            key = "entities." + special[2];
        }
        // usage
        lines.add(Text.literal(text).styled(style -> style.withColor(Formatting.YELLOW).withBold(true)));
        // desc
        if (tag.equals("special_item")){
            // 處裡賽季武器的玩意
            if (special[2].contains("season")){
                String[] season = special[2].split("\\.");
                String item = season[2];
                if (item.contains("_0") || item.contains("_1")){
                    item = item.substring(0,item.indexOf("_",-1));
                }
                key = "items.seasons.nameless." + item;
                lines.add(
                    Text.translatable("items.seasons.common", season[1], Text.translatable(key))
                    .styled(style -> style.withColor(Formatting.WHITE))
                );
            }else{
                lines.add(
                    Text.translatable("items.common",Text.translatable(key))
                    .styled(style -> style.withColor(Formatting.WHITE))
                );
            }
        }
        if (tag.equals("special_entity")){
            lines.add(
                Text.translatable("entities.common",Text.translatable(key))
                .styled(style -> style.withColor(Formatting.WHITE))
            );
        }
        return lines;
    }
    // /buff /config /land config 專用info
    private static List<Text> configCommandInfo(String text, String tag) {
        List<Text> lines = new ArrayList<>();
        String[] config = text.split(" ");
        String key = "";
        if (tag.equals("buff")) {
            key = "buff." + config[1];
            // usage
            lines.add(
                Text.literal("/buff " + config[1]).append(" [true/false/def]")
                .styled(style -> style.withColor(Formatting.YELLOW).withBold(true))
            );
            // desc
            lines.add(Text.translatable("buff.common",Text.translatable(key)).styled(style -> style.withColor(Formatting.WHITE)));
        }
        if (tag.equals("config")) {
            key = "config." + config[1];
            // usage
            lines.add(
                Text.literal("/config " + config[1]).append(" [true/false/def]")
                .styled(style -> style.withColor(Formatting.YELLOW).withBold(true))
            );
            // desc
            lines.add(Text.translatable("config.common",Text.translatable(key)).styled(style -> style.withColor(Formatting.WHITE)));
        }
        if (tag.equals("land")) {
            key = "land." + config[3];
            // usage
            lines.add(
                Text.literal("/land config <領地編號> " + config[3]).append(" [true/false/def]")
                .styled(style -> style.withColor(Formatting.YELLOW).withBold(true))
            );
            // desc
            lines.add(Text.translatable("config.common",Text.translatable(key)).styled(style -> style.withColor(Formatting.WHITE)));
        }

        lines.add(Text.literal("true:開啟;false:關閉;def:預設值").styled(style -> style.withColor(Formatting.WHITE)));
        return lines;
    }
    // /mode 專用info
    private static List<Text> modeCommandInfo(String text) {
        List<Text> lines = new ArrayList<>();
        String[] mode = text.split(" ");
        String key = mode[2];
        // usage
        lines.add(
            Text.literal(text).styled(style -> style.withColor(Formatting.YELLOW).withBold(true))
        );
        // desc
        lines.add(
            Text.translatable("mode.common",Text.translatable(key))
            .styled(style -> style.withColor(Formatting.WHITE))
        );
        return lines;
    }
    // 檢查有沒有特殊info
    private static boolean checkWildCardInfo(CommandInfo info){
        if (info.getName().equals("*")){
            return true;
        }
        return false;
    }
    public static void render(DrawContext context, Screen screen) {
        CatBudConfig config = CatBudConfig.getInstance();
        if (!config.showCommandHelp) {
            return;
        }

        if (!(screen instanceof ChatScreen chatScreen)) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getWindow() == null) {
            return;
        }

        TextFieldWidget chatField = ((ChatScreenAccessor) chatScreen).catbud$getChatField();
        if (chatField == null) {
            return;
        }

        String text = chatField.getText();
        if (text == null || text.isBlank() || !text.startsWith("/")) {
            return;
        }

        CatBudCommandRegistry.MatchResult matchResult = CatBudCommandRegistry.findMatch(text);
        if (matchResult == null || matchResult.commandInfo == null) {
            return;
        }

        CommandInfo info = matchResult.commandInfo;

        // 準備要繪製的文字行數
        List<Text> lines = new ArrayList<>();
        if (checkWildCardInfo(info)){
            String tag = info.getTag();
            if (tag.equals("buff") || tag.equals("config") || tag.equals("land")){
                lines.addAll(configCommandInfo(text, tag));
            }
            if (tag.equals("mode")){
                lines.addAll(modeCommandInfo(text));
            }
            if (tag.contains("special")){
                lines.addAll(specialCommandInfo(text, tag));
            }
        }
        if (!checkWildCardInfo(info)) {
            lines.addAll(regularCommandInfo(info));
        }

        if (lines.isEmpty()) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        int maxLineWidth = 0;
        for (Text line : lines) {
            maxLineWidth = Math.max(maxLineWidth, textRenderer.getWidth(line));
        }

        int width = maxLineWidth + 12;
        int height = lines.size() * 11 + 6;

        // 計算基準 X 與 Y
        int x = Math.max(4, chatField.getX() + 2);
        int scaledWidth = client.getWindow().getScaledWidth();
        if (x + width > scaledWidth - 4) {
            x = Math.max(4, scaledWidth - width - 4);
        }

        int baseY = chatField.getY() - 6;

        // 檢查原生 ChatInputSuggestor 補全選單是否存在
        ChatInputSuggestor suggestor = ((ChatScreenAccessor) chatScreen).catbud$getChatInputSuggestor();
        if (suggestor != null) {
            ChatInputSuggestor.SuggestionWindow window = ((ChatInputSuggestorAccessor) suggestor).catbud$getWindow();
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
            context.drawTextWithShadow(textRenderer, lines.get(i), x, y + i * 11, 0xFFFFFFFF);
        }
    }
}
