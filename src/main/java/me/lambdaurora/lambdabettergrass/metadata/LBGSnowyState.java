/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
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
import me.lambdaurora.lambdabettergrass.model.LBGSnowyUnbakedModel;
import me.lambdaurora.lambdabettergrass.util.SnowUtils;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.StringReader;
import java.util.function.Function;

public class LBGSnowyState extends LBGState
{
    private final boolean                                snowLayer;
    private final Object2ObjectMap<String, UnbakedModel> variantModels = new Object2ObjectOpenHashMap<>();
    private       UnbakedModel                           alternateModel;
    private final boolean                                snowyModel;


    public LBGSnowyState(@NotNull Identifier id, @NotNull ResourceManager resourceManager, @NotNull JsonObject json, @NotNull ModelVariantMap.DeserializationContext deserializationContext)
    {
        super(id);

        if (json.has("snow_layer")) {
            this.snowLayer = json.get("snow_layer").getAsBoolean();
        } else {
            this.snowLayer = false;
        }

        if (!json.has("block_state")) {
            this.alternateModel = null;
            this.snowyModel = false;
            return;
        }

        ModelVariantMap map = ModelVariantMap.deserialize(deserializationContext, new StringReader(json.get("block_state").toString()));
        if (map.hasMultipartModel())
            this.alternateModel = map.getMultipartModel();
        else
            this.variantModels.putAll(map.getVariantMap());

        this.snowyModel = true;
    }

    @Override
    public @Nullable UnbakedModel getCustomUnbakedModel(@NotNull ModelIdentifier modelId, @NotNull UnbakedModel originalModel, @NotNull Function<Identifier, UnbakedModel> modelGetter)
    {
        UnbakedModel snowLayerModel = null;
        if (this.snowLayer) {
            snowLayerModel = SnowUtils.getSnowLayerModel(modelGetter);
        }

        UnbakedModel snowyModel = null;
        if (this.snowyModel) {
            if (this.alternateModel != null) {
                this.alternateModel.getModelDependencies().forEach(modelGetter::apply);
                snowyModel = this.alternateModel;
            } else {
                UnbakedModel snowyVariantModel = this.variantModels.get(modelId.getVariant());
                if (snowyVariantModel != null) {
                    snowyVariantModel.getModelDependencies().forEach(modelGetter::apply);
                    snowyModel = snowyVariantModel;
                }
            }
        }

        if (snowLayerModel == null && snowyModel == null)
            return null;
        return new LBGSnowyUnbakedModel(originalModel, snowyModel, snowLayerModel);
    }
}
