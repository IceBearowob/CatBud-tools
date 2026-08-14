package ice.catbudtools.client.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ice.catbudtools.CatBudTools;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public final class CatBudCommandRegistry {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, CommandInfo> COMMANDS = new HashMap<>();
    private static boolean loaded = false;

    private CatBudCommandRegistry() {
    }

    public static void load() {
        COMMANDS.clear();

        // 1. 先嘗試從內建資源 /assets/catbud-tools/commands.json 載入 (使用 ClassLoader 避免 MinecraftClient 尚未初始化完成)
        try (InputStream stream = CatBudCommandRegistry.class.getResourceAsStream("/assets/catbud-tools/commands.json")) {
            if (stream != null) {
                try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    parseAndRegister(reader);
                }
            } else {
                CatBudTools.LOGGER.warn("[CatBud Tools] 找不到內建 commands.json 資源檔");
            }
        } catch (Exception e) {
            CatBudTools.LOGGER.warn("[CatBud Tools] 無法從資源載入 commands.json: " + e.getMessage());
        }

        loaded = true;
    }

    private static void parseAndRegister(InputStreamReader reader) {
        Type type = new TypeToken<Map<String, CommandInfo>>() {}.getType();
        Map<String, CommandInfo> parsed = GSON.fromJson(reader, type);
        if (parsed != null) {
            for (Map.Entry<String, CommandInfo> entry : parsed.entrySet()) {
                String cmdName = entry.getKey().toLowerCase();
                CommandInfo info = entry.getValue();
                info.setName(cmdName);
                processCommandInfo(info, cmdName);
                COMMANDS.put(cmdName, info);
            }
        }
    }

    private static void processCommandInfo(CommandInfo info, String name) {
        if (info.getUsage() != null) {
            info.setUsage(info.getUsage());
        }
        if (info.getDescription() != null) {
            info.setDescription(info.getDescription());
        }

        if (info.getSubcommands() != null) {
            for (Map.Entry<String, CommandInfo> subEntry : info.getSubcommands().entrySet()) {
                String subName = subEntry.getKey().toLowerCase();
                CommandInfo subInfo = subEntry.getValue();
                subInfo.setName(subName);
                processCommandInfo(subInfo, subName);
            }
        }

        if (info.getTag() != null){
            info.setTag(info.getTag());
        }
    }

    public static MatchResult findMatch(String rawInput) {
        if (!loaded) {
            load();
        }
        if (rawInput == null || rawInput.isBlank()) {
            return null;
        }

        String input = rawInput.trim();
        if (input.startsWith("/")) {
            input = input.substring(1);
        }
        if (input.isEmpty()) {
            return null;
        }

        String[] parts = input.split("\\s+");
        if (parts.length == 0) {
            return null;
        }

        String rootCmd = parts[0].toLowerCase();
        CommandInfo current = COMMANDS.get(rootCmd);
        if (current == null) {
            return null;
        }

        CommandInfo bestMatch = current;
        int matchedTokens = 1;

        for (int i = 1; i < parts.length; i++) {
            String subToken = parts[i].toLowerCase();
            Map<String, CommandInfo> subs = current.getSubcommands();
            if (subs != null && subs.containsKey(subToken)) {
                // 精確匹配子指令
                current = subs.get(subToken);
                bestMatch = current;
                matchedTokens++;
            } else if (subs != null && subs.containsKey("*")) {
                // 萬用字元：當前 token 為動態參數，跳過並繼續比對
                CommandInfo wildcard = subs.get("*");
                current = wildcard;
                // 只有萬用字元節點本身有說明時才更新 bestMatch
                String wUsage = wildcard.getUsage();
                List<String> wDesc = wildcard.getDescription();
                String wTag = wildcard.getTag();
                if ((wUsage != null && !wUsage.isBlank()) || (wDesc != null && !wDesc.isEmpty() || (wTag != null && !wTag.isBlank()))) {
                    bestMatch = wildcard;
                }
                matchedTokens++;
            } else {
                break;
            }
        }

        return new MatchResult(bestMatch, matchedTokens, parts.length);
    }

    public static class MatchResult {
        public final CommandInfo commandInfo;
        public final int matchedTokens;
        public final int totalTokens;

        public MatchResult(CommandInfo commandInfo, int matchedTokens, int totalTokens) {
            this.commandInfo = commandInfo;
            this.matchedTokens = matchedTokens;
            this.totalTokens = totalTokens;
        }
    }
}
