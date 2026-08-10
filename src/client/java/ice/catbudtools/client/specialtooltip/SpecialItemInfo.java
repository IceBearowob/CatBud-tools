package ice.catbudtools.client.specialtooltip;

import java.util.Collections;
import java.util.List;

/**
 * 特附（特殊附魔）的 JSON 資料映射類別。
 * 對應 special_enchants.json 中每個特附條目的結構。
 */
public class SpecialItemInfo {

    private String name;
    /** 特附說明文字（多行），在 Tooltip 中以灰色顯示。 */
    private List<String> lore;

    /**
     * 衝突附魔文字列表（多行），
     * 只有在附魔書上才顯示，以灰色顯示。
     * 內容為面向玩家的說明文字，例如「▶ 衝突附魔：無限耐久」。
     */
    private List<String> tip;

    public SpecialItemInfo() {}

    public String getname() {
        return name;
    }

    public List<String> getLore() {
        return lore != null ? lore : Collections.emptyList();
    }

    public List<String> getTip() {
        return tip != null ? tip : Collections.emptyList();
    }
}
