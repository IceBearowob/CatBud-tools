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
        } else {
            lines.add(Text.literal("/" + info.getName()).styled(style -> style.withColor(Formatting.YELLOW).withBold(true)));
        }

        // 2. 指令說明 Description
        if (info.getDescription() != null && !info.getDescription().isEmpty()) {
            for (String desc : info.getDescription()){
                lines.add(Text.literal(desc).styled(style -> style.withColor(Formatting.WHITE)));
            }
        }
        return lines;
    }
    // 給 /special 專用的info
    private static List<Text> specialCommandInfo(String text) {
        List<Text> lines = new ArrayList<>();
        String[] special = text.split(" ");
        String key = special[2];
        // humanoid_armor_stand同時有item跟entity的形式(
        if (special[1].equals("entity") && special[2].equals("humanoid_armor_stand")){
            key = "entities.humanoid_armor_stand";
        }
        // usage
        lines.add(Text.literal(text).styled(style -> style.withColor(Formatting.YELLOW).withBold(true)));
        // desc
        if (special[1].equals("item")){
            // 處裡賽季武器的玩意
            if (special[2].contains("season")){
                String[] season = special[2].split("\\.");
                String item = season[2];
                if (item.contains("_0") || item.contains("_1")){
                    item = item.substring(0,item.indexOf("_",-1));
                }
                key = "seasons.nameless." + item;
                lines.add(
                    Text.literal("給予物品" + "\"")
                    .append("第" + season[1] + "賽季限定")
                    .append(Text.translatable(key))
                    .append("\"")
                    .styled(style -> style.withColor(Formatting.WHITE))
                );
            }else{
                lines.add(
                    Text.literal("給予物品\"")
                    .append(Text.translatable(key))
                    .append("\"")
                    .styled(style -> style.withColor(Formatting.WHITE))
                );
            }
        }
        if (special[1].equals("entity")){
            lines.add(
                Text.literal("召喚實體\"")
                .append(Text.translatable(key))
                .append(Text.literal("\""))
                .styled(style -> style.withColor(Formatting.WHITE))
            );
        }
        return lines;
    }
    // 檢查是不是Special Command(同時還得是有參數的)
    // 檢查1.是不是/special 2.是不是3段 3.有沒有翻譯(又分有沒有season)
    private static boolean isSpecialCommand(String text) {
        if (text.contains("/special")){
            String[] special = text.split(" ");
            if (special.length != 3){
                return false;
            }
            if (I18n.hasTranslation(special[2])){
                return true;
            }
            if (special[2].contains("season")){
                if (special[2].split("\\.").length == 3){
                    return true;
                }
                return false;
            }
            
            return false;
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
        if (isSpecialCommand(text)){
            lines.addAll(specialCommandInfo(text));
        }else{
            //基礎值
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
