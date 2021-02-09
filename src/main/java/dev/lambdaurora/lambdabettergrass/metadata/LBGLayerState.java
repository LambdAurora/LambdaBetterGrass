/*
 * Copyright © 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.lambdaurora.lambdabettergrass.model.LBGLayerUnbakedModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aperlambda.lambdacommon.LambdaConstants;
import org.aperlambda.lambdacommon.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents model states, which have layered connection with blocks like snow, with its different {@link LBGLayerMetadata}.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public class LBGLayerState extends LBGState {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<String, List<LBGLayerMetadata>> metadatas = new Object2ObjectOpenHashMap<>();

    public LBGLayerState(Identifier id, ResourceManager resourceManager, JsonObject json, ModelVariantMap.DeserializationContext deserializationContext) {
        super(id);

        if (json.has("variants")) {
            JsonObject variants = json.getAsJsonObject("variants");
            variants.entrySet().forEach(entry -> {
                JsonObject variant = entry.getValue().getAsJsonObject();
                if (variant.has("data")) {
                    this.loadVariant(entry.getKey(), variant, resourceManager, deserializationContext);
                }
            });
        } else if (json.has("data")){
            this.loadVariant("*", json, resourceManager, deserializationContext);
        } else {
            LOGGER.warn("Invalid state definition for {}, missing data or variants entry.", id);
        }
    }

    private void loadVariant(String variant, JsonObject json, ResourceManager resourceManager, ModelVariantMap.DeserializationContext deserializationContext) {
        Identifier metadataId = Identifier.tryParse(json.get("data").getAsString());
        Identifier metadataResourceId = new Identifier(id.getNamespace(), metadataId.getPath() + ".json");
        try {
            List<Resource> resources = resourceManager.getAllResources(metadataResourceId);
            for (Resource resource : resources) {
                JsonObject metadataJson = LambdaConstants.JSON_PARSER.parse(new InputStreamReader(resource.getInputStream())).getAsJsonObject();

                for (Map.Entry<String, JsonElement> entry : metadataJson.entrySet()) {
                    LBGLayerType type = LBGLayerType.fromName(entry.getKey());

                    if (type == null)
                        continue;

                    if (entry.getValue().isJsonObject()) {
                        this.putOrReplaceMetadata(variant, metadataId, type, entry.getValue().getAsJsonObject(), deserializationContext);
                    }
                }

                resource.close();
            }
        } catch (IOException e) {
            LOGGER.warn("Cannot load metadata file \"" + metadataId + "\" from layer state \"" + id + "\" (variant: \"" + variant + "\").", e);
        }
    }

    private void putOrReplaceMetadata(String variant, Identifier metadataId, LBGLayerType type, JsonObject metadataJson, ModelVariantMap.DeserializationContext deserializationContext) {
        List<LBGLayerMetadata> metadatas = this.metadatas.computeIfAbsent(variant, v -> new ArrayList<>());
        Iterator<LBGLayerMetadata> it = metadatas.iterator();
        while (it.hasNext()) {
            LBGLayerMetadata next = it.next();

            if (next.layerType == type) {
                it.remove();
                break;
            }
        }

        metadatas.add(new LBGLayerMetadata(metadataId, type, metadataJson, deserializationContext));
    }

    public void forEach(String[] variant, Consumer<LBGLayerMetadata> consumer) {
        for (Map.Entry<String, List<LBGLayerMetadata>> entry : this.metadatas.entrySet()) {
            if (this.matchVariant(variant, entry.getKey().split(","))) {
                entry.getValue().forEach(consumer);
                return;
            }
        }
    }

    @Override
    public @Nullable UnbakedModel getCustomUnbakedModel(@NotNull ModelIdentifier modelId, @NotNull UnbakedModel originalModel, @NotNull Function<Identifier, UnbakedModel> modelGetter) {
        String[] modelVariant = modelId.getVariant().split(",");

        for (Map.Entry<String, List<LBGLayerMetadata>> entry : this.metadatas.entrySet()) {
            if (entry.getKey().equals("*") || this.matchVariant(modelVariant, entry.getKey().split(","))) {
                List<LBGCompiledLayerMetadata> metadatas = new ArrayList<>();

                entry.getValue().forEach(metadata -> {
                    Pair<UnbakedModel, UnbakedModel> models = metadata.getCustomUnbakedModel(modelId, originalModel, modelGetter);
                    if (models.key == null && models.value == null)
                        return;

                    metadatas.add(new LBGCompiledLayerMetadata(metadata.layerType, models.key, models.value));
                });

                if (metadatas.size() != 0) {
                    return new LBGLayerUnbakedModel(originalModel, metadatas);
                }

                return null;
            }
        }
        return null;
    }
}
