package ice.catbudtools.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

/**
 * 貓芽工具模組的設定資料類別，負責 JSON 設定檔 (catbud-tools.json) 的讀取與儲存。
 */
public class CatBudConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("catbud-tools.json");
	public static final int DEFAULT_OFFSET_X = 14;
	public static final int DEFAULT_OFFSET_Y = 14;
	public static final TooltipPosition DEFAULT_TOOLTIP_POSITION = TooltipPosition.FOLLOW_MOUSE;
	public static final boolean DEFAULT_ALWAYSTOOLTIP = false;
	public static final boolean DEFAULT_SHOWTOOLTIP = true;
	public static final int DEFAULT_MAX_DISPLAY_ENCHANT = 0;

	public TooltipPosition tooltipPosition = TooltipPosition.FOLLOW_MOUSE;
	public int offsetX = 14;
	public int offsetY = 14;
	public boolean AlwaysShowTooltip = false;
	public boolean ShowTooltip = true;
	public int max_display_enchant = 0;

	private static CatBudConfig INSTANCE = new CatBudConfig();

	public static CatBudConfig getInstance() {
		return INSTANCE;
	}
	public void reset() {

		this.offsetX = DEFAULT_OFFSET_X;
		this.offsetY = DEFAULT_OFFSET_Y;
		this.tooltipPosition = DEFAULT_TOOLTIP_POSITION;

	}
	public static void load() {
		if (!CONFIG_FILE.toFile().exists()) {
			save();
			return;
		}
		try (FileReader reader = new FileReader(CONFIG_FILE.toFile())) {
			CatBudConfig loaded = GSON.fromJson(reader, CatBudConfig.class);
			if (loaded != null) {
				INSTANCE = loaded;
			}
		} catch (Exception e) {
			e.printStackTrace();
			INSTANCE = new CatBudConfig();
		}
	}

	public static void save() {
		try (FileWriter writer = new FileWriter(CONFIG_FILE.toFile())) {
			GSON.toJson(INSTANCE, writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
