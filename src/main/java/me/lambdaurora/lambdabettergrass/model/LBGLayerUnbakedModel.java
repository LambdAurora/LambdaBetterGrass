/*
 * Copyright © 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdabettergrass.model;

import com.mojang.datafixers.util.Pair;
import me.lambdaurora.lambdabettergrass.metadata.LBGCompiledLayerMetadata;
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
 * Represents the LambdaBetterGrass unbaked model for layer method.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LBGLayerUnbakedModel implements UnbakedModel {
    private final UnbakedModel baseModel;
    private final List<LBGCompiledLayerMetadata> metadatas;

    public LBGLayerUnbakedModel(@NotNull UnbakedModel baseModel, @NotNull List<LBGCompiledLayerMetadata> metadatas) {
        this.baseModel = baseModel;
        this.metadatas = metadatas;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        Set<Identifier> ids = new HashSet<>(this.baseModel.getModelDependencies());
        this.metadatas.forEach(metadata -> metadata.fetchModelDependencies(ids));
        return ids;
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        List<SpriteIdentifier> ids = new ArrayList<>(this.baseModel.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences));
        this.metadatas.forEach(metadata -> metadata.fetchTextureDependencies(ids, unbakedModelGetter, unresolvedTextureReferences));
        return ids;
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        this.metadatas.forEach(metadata -> metadata.bake(loader, textureGetter, rotationContainer, modelId));
        return new LBGLayerBakedModel(Objects.requireNonNull(this.baseModel.bake(loader, textureGetter, rotationContainer, modelId)), this.metadatas);
    }
}
