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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents the LambdaBetterGrass unbaked model for layer method.
 *
 * @author LambdAurora
 * @version 2.1.0
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
	public void resolveDependencies(Resolver resolver) {
		this.baseModel.resolveDependencies(resolver);
		this.metadatas.forEach(metadata -> metadata.resolveModelDependencies(resolver));
	}

	@Override
	public @NotNull BakedModel bake(
			ModelBaker baker, Function<Material, TextureAtlasSprite> textureGetter,
			ModelState modelState
	) {
		this.metadatas.forEach(metadata -> metadata.bake(baker, textureGetter, modelState));
		return new LBGLayerBakedModel(Objects.requireNonNull(this.baseModel.bake(baker, textureGetter, modelState)),
				this.metadatas
		);
	}
}
