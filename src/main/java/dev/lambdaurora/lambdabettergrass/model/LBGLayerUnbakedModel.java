/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.model;

import dev.lambdaurora.lambdabettergrass.metadata.layer.LBGCompiledLayerMetadata;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Represents the LambdaBetterGrass unbaked model for layer method.
 *
 * @author LambdAurora
 * @version 1.4.0
 * @since 1.0.0
 */
public class LBGLayerUnbakedModel implements UnbakedModel {
	private final UnbakedModel baseModel;
	private final List<LBGCompiledLayerMetadata> metadatas;

	public LBGLayerUnbakedModel(UnbakedModel baseModel, List<LBGCompiledLayerMetadata> metadatas) {
		this.baseModel = baseModel;
		this.metadatas = metadatas;
	}

	@Override
	public Collection<Identifier> getDependencies() {
		Set<Identifier> ids = new HashSet<>(this.baseModel.getDependencies());
		this.metadatas.forEach(metadata -> metadata.fetchModelDependencies(ids));
		return ids;
	}

	@Override
	public void resolveParents(Function<Identifier, UnbakedModel> models) {
		this.baseModel.resolveParents(models);
		this.metadatas.forEach(metadata -> metadata.resolveParents(models));
	}

	@Override
	public @Nullable BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> textureGetter,
			ModelState rotationContainer, Identifier modelId) {
		this.metadatas.forEach(metadata -> metadata.bake(baker, textureGetter, rotationContainer, modelId));
		return new LBGLayerBakedModel(Objects.requireNonNull(this.baseModel.bake(baker, textureGetter, rotationContainer, modelId)),
				this.metadatas
		);
	}
}
