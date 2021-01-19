/*
 * Copyright © 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdabettergrass.metadata;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.aperlambda.lambdacommon.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.StringReader;
import java.util.function.Function;

/**
 * Represents a metadata for blocks which have snowy variants or equivalent.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LBGLayerMetadata {
    public final Identifier id;
    public final LBGLayerType layerType;
    private final boolean layerModel;
    private final Object2ObjectMap<String, UnbakedModel> variantModels = new Object2ObjectOpenHashMap<>();
    private UnbakedModel alternateModel;
    private final boolean hasAlternateModel;

    public LBGLayerMetadata(@NotNull Identifier id, LBGLayerType layerType, @NotNull JsonObject json, @NotNull ModelVariantMap.DeserializationContext deserializationContext) {
        this.id = id;
        this.layerType = layerType;

        if (json.has("layer")) {
            this.layerModel = json.get("layer").getAsBoolean();
        } else {
            this.layerModel = false;
        }

        if (!json.has("block_state")) {
            this.alternateModel = null;
            this.hasAlternateModel = false;
            return;
        }

        ModelVariantMap map = ModelVariantMap.deserialize(deserializationContext, new StringReader(json.get("block_state").toString()));
        if (map.hasMultipartModel())
            this.alternateModel = map.getMultipartModel();
        else
            this.variantModels.putAll(map.getVariantMap());

        this.hasAlternateModel = true;
    }

    public @NotNull Pair<UnbakedModel, UnbakedModel> getCustomUnbakedModel(@NotNull ModelIdentifier modelId, @NotNull UnbakedModel originalModel, @NotNull Function<Identifier, UnbakedModel> modelGetter) {
        UnbakedModel layerModel = null;
        if (this.layerModel) {
            layerModel = this.layerType.getLayerModel(modelGetter);
        }

        UnbakedModel alternateModel = null;
        if (this.hasAlternateModel) {
            if (this.alternateModel != null) {
                alternateModel = this.alternateModel;
            } else {
                UnbakedModel alternateVariantModel = this.variantModels.get(modelId.getVariant());
                if (alternateVariantModel != null) {
                    alternateModel = alternateVariantModel;
                }
            }
        }

        return Pair.of(layerModel, alternateModel);
    }

    @Override
    public String toString() {
        return "LBGLayerMetadata{" +
                "id=" + id +
                ", layerType=" + layerType +
                ", layerModel=" + layerModel +
                ", hasAlternateModel=" + hasAlternateModel +
                '}';
    }
}
