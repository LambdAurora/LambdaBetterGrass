/*
 * Copyright © 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata;

import com.google.gson.JsonObject;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.model.LBGLayerUnbakedModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.aperlambda.lambdacommon.LambdaConstants;
import org.aperlambda.lambdacommon.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents model states, which have layered connection with blocks like snow, with its different {@link LBGLayerMetadata}.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LBGLayerState extends LBGState {
    private final List<LBGLayerMetadata> metadatas = new ArrayList<>();

    public LBGLayerState(@NotNull Identifier id, @NotNull ResourceManager resourceManager, @NotNull JsonObject json, @NotNull ModelVariantMap.DeserializationContext deserializationContext) {
        super(id);

        LBGLayerType.forEach(layerType -> {
            String[] path = id.getPath().split("/");
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < path.length - 1; i++)
                stringBuilder.append(path[i]).append("/");
            Identifier metadataId = new Identifier(id.getNamespace(), stringBuilder.toString() + layerType.getName() + "/" + path[path.length - 1]);
            Identifier metadataResourceId = new Identifier(id.getNamespace(), metadataId.getPath() + ".json");
            if (resourceManager.containsResource(metadataResourceId)) {
                try {
                    InputStream stream = resourceManager.getResource(metadataResourceId).getInputStream();
                    JsonObject metadataJson = LambdaConstants.JSON_PARSER.parse(new InputStreamReader(stream)).getAsJsonObject();

                    this.metadatas.add(new LBGLayerMetadata(metadataId, layerType, metadataJson, deserializationContext));

                    stream.close();
                } catch (IOException e) {
                    LambdaBetterGrass.get().warn("Cannot load metadata file \"" + metadataId + "\" from layer state \"" + id + "\".");
                }
            }
        });
    }

    public void forEach(@NotNull Consumer<LBGLayerMetadata> consumer) {
        this.metadatas.forEach(consumer);
    }

    @Override
    public @Nullable UnbakedModel getCustomUnbakedModel(@NotNull ModelIdentifier modelId, @NotNull UnbakedModel originalModel, @NotNull Function<Identifier, UnbakedModel> modelGetter) {
        List<LBGCompiledLayerMetadata> metadatas = new ArrayList<>();
        this.forEach(metadata -> {
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
