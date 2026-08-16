package ice.catbudtools.client.specialtooltip;

import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 一個 Tooltip 區塊。
 * 例如：
 * - 一個特殊附魔（名稱 + Lore）
 * - 一個特殊物品資訊
 */
public class TooltipSection {

    public enum Type {
        ENCHANT,
        SPECIAL_ITEM
    }

    private final Type type;
    private final List<Component> lines;

    public TooltipSection(Type type, List<Component> lines) {
        this.type = type;
        this.lines = lines;
    }

    public Type getType() {
        return type;
    }

    public List<Component> getLines() {
        return lines;
    }
}