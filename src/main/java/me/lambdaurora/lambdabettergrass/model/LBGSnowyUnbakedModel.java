/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdabettergrass.model;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Represents the LambdaBetterGrass unbaked model for snowy method.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LBGSnowyUnbakedModel implements UnbakedModel
{
    private final UnbakedModel baseModel;
    private final UnbakedModel snowyModel;
    private final UnbakedModel snowLayerModel;

    public LBGSnowyUnbakedModel(@NotNull UnbakedModel baseModel, @Nullable UnbakedModel snowyModel, @Nullable UnbakedModel snowLayerModel)
    {
        this.baseModel = baseModel;
        this.snowyModel = snowyModel;
        this.snowLayerModel = snowLayerModel;
    }

    @Override
    public Collection<Identifier> getModelDependencies()
    {
        return Collections.emptySet();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences)
    {
        List<SpriteIdentifier> ids = new ArrayList<>(this.baseModel.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences));
        if (this.snowyModel != null)
            ids.addAll(this.snowyModel.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences));
        if (this.snowLayerModel != null)
            ids.addAll(this.snowLayerModel.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences));
        return ids;
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId)
    {
        BakedModel snowyModel = null;
        if (this.snowyModel != null)
            snowyModel = this.snowyModel.bake(loader, textureGetter, rotationContainer, modelId);
        BakedModel snowLayerModel = null;
        if (this.snowLayerModel != null)
            snowLayerModel = this.snowLayerModel.bake(loader, textureGetter, rotationContainer, modelId);
        return new LBGSnowyBakedModel(Objects.requireNonNull(this.baseModel.bake(loader, textureGetter, rotationContainer, modelId)),
                snowyModel, snowLayerModel);
    }
}
