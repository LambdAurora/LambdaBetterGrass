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
import me.lambdaurora.lambdabettergrass.metadata.LBGMetadata;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

/**
 * Represents the LambdaBetterGrass unbaked model.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LBGUnbakedModel implements UnbakedModel
{
    private final UnbakedModel baseModel;
    private final LBGMetadata  metadata;

    public LBGUnbakedModel(@NotNull UnbakedModel baseModel, @NotNull LBGMetadata metadata)
    {
        this.baseModel = baseModel;
        this.metadata = metadata;
    }

    @Override
    public Collection<Identifier> getModelDependencies()
    {
        return Collections.emptySet();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences)
    {
        Collection<SpriteIdentifier> baseIds = this.baseModel.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences);
        List<SpriteIdentifier> textures = new ArrayList<>(baseIds);
        textures.addAll(this.metadata.getTextures());
        if (this.metadata.getSnowyVariant() != null)
            textures.addAll(this.metadata.getSnowyVariant().getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences));
        return textures;
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId)
    {
        this.metadata.bakeTextures(textureGetter);

        LBGBakedModel model = new LBGBakedModel(Objects.requireNonNull(this.baseModel.bake(loader, textureGetter, rotationContainer, modelId)), this.metadata);

        this.metadata.propagate(model);

        return model;
    }
}
