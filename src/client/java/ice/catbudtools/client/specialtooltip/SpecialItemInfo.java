package ice.catbudtools.client.specialtooltip;

import java.util.Collections;
import java.util.List;

public class SpecialItemInfo {

    private String name;
    private List<String> lore;
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
