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
import me.lambdaurora.lambdabettergrass.model.LBGSnowLayerUnbakedModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Represents snow model states.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LBGSnowLayerState extends LBGState
{
    public LBGSnowLayerState(@NotNull Identifier id, @NotNull ResourceManager resourceManager, @NotNull JsonObject json)
    {
        super(id);
    }

    private @Nullable UnbakedModel getSnowLayerModel(@NotNull Function<Identifier, UnbakedModel> modelGetter)
    {
        return modelGetter.apply(new ModelIdentifier(new Identifier("minecraft:snow"), "layers=1"));
    }

    @Override
    public @Nullable UnbakedModel getCustomUnbakedModel(@NotNull ModelIdentifier modelId, @NotNull UnbakedModel originalModel, @NotNull Function<Identifier, UnbakedModel> modelGetter)
    {
        UnbakedModel snowLayerModel = this.getSnowLayerModel(modelGetter);
        if (snowLayerModel != null)
            return new LBGSnowLayerUnbakedModel(originalModel, snowLayerModel);
        return null;
    }
}
