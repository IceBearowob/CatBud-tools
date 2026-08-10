package ice.catbudtools.client.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CommandInfo {
    private String name;
    private String usage;
    private String description;
    private Map<String, CommandInfo> subcommands = new HashMap<>();

    public CommandInfo() {
    }

    public CommandInfo(String name, String usage, String description) {
        this.name = name;
        this.usage = usage;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, CommandInfo> getSubcommands() {
        return subcommands != null ? subcommands : Collections.emptyMap();
    }

    public void setSubcommands(Map<String, CommandInfo> subcommands) {
        this.subcommands = subcommands;
    }
}
