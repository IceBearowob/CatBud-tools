package ice.catbudtools.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ice.catbudtools.CatBudTools;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;


public class CatBudLanguageReloadListener
        implements SynchronousResourceReloader {


    @Override
    public void reload(
            ResourceManager manager
    ) {


        TranslationOverride.clear();


        Identifier id =
                Identifier.of(
                        "minecraft",
                        "lang/zh_tw.json"
                );


        manager.getResource(id)
                .ifPresent(resource -> {

                    try {

                        JsonObject json =
                                JsonParser.parseReader(
                                        new InputStreamReader(
                                                resource.getInputStream()
                                        )
                                )
                                .getAsJsonObject();


                        for (String key : json.keySet()) {

                            TranslationOverride.put(
                                    key,
                                    json.get(key)
                                            .getAsString()
                            );
                        }


                        CatBudTools.LOGGER.info(
                                "Loaded translations: {}",
                                json.size()
                        );


                    } catch (Exception e) {

                        CatBudTools.LOGGER.error(
                                "Failed loading zh_tw.json",
                                e
                        );

                    }

                });
    }
}   