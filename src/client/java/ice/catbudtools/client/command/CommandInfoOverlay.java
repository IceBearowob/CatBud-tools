package ice.catbudtools.client.command;

import ice.catbudtools.client.config.CatBudConfig;
import ice.catbudtools.client.mixin.ChatScreenAccessor;
import ice.catbudtools.client.mixin.ChatInputSuggestorAccessor;
import ice.catbudtools.client.mixin.SuggestionWindowAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CommandInfoOverlay {

    private CommandInfoOverlay() {
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

        // 1. 指令語法 Usage (標題)
        if (info.getUsage() != null && !info.getUsage().isEmpty()) {
            lines.add(Text.literal(info.getUsage()).styled(style -> style.withColor(Formatting.YELLOW).withBold(true)));
        } else {
            lines.add(Text.literal("/" + info.getName()).styled(style -> style.withColor(Formatting.YELLOW).withBold(true)));
        }

        // 2. 指令說明 Description
        if (info.getDescription() != null && !info.getDescription().isEmpty()) {
            lines.add(Text.literal(info.getDescription()).styled(style -> style.withColor(Formatting.WHITE)));
        }

        // 3. 若有子指令且玩家尚未輸入完子指令，顯示可用子指令提示
        Map<String, CommandInfo> subcommands = info.getSubcommands();
        if (!subcommands.isEmpty()) {
            lines.add(Text.literal("可用子指令:").styled(style -> style.withColor(Formatting.GOLD)));
            for (Map.Entry<String, CommandInfo> entry : subcommands.entrySet()) {
                CommandInfo sub = entry.getValue();
                String subUsage = sub.getUsage() != null ? sub.getUsage() : sub.getName();
                String subDesc = sub.getDescription() != null ? " - " + sub.getDescription() : "";
                lines.add(Text.literal("  " + subUsage + subDesc).styled(style -> style.withColor(Formatting.GRAY)));
            }
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
