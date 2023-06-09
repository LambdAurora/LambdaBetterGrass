/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.model;

import dev.lambdaurora.lambdabettergrass.metadata.LBGMetadata;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents the LambdaBetterGrass unbaked model.
 *
 * @author LambdAurora
 * @version 1.4.0
 * @since 1.0.0
 */
public class LBGUnbakedModel implements UnbakedModel {
	private final UnbakedModel baseModel;
	private final LBGMetadata metadata;

	public LBGUnbakedModel(UnbakedModel baseModel, LBGMetadata metadata) {
		this.baseModel = baseModel;
		this.metadata = metadata;
	}

	@Override
	public Collection<Identifier> getModelDependencies() {
		return this.baseModel.getModelDependencies();
	}

	@Override
	public void resolveParents(Function<Identifier, UnbakedModel> models) {
		this.baseModel.resolveParents(models);

		if (this.metadata.getSnowyVariant() != null) {
			this.metadata.getSnowyVariant().resolveParents(models);
		}
	}

	@Override
	public @Nullable BakedModel bake(ModelBaker baker, Function<SpriteIdentifier, Sprite> textureGetter,
			ModelBakeSettings rotationContainer, Identifier modelId) {
		this.metadata.bakeTextures(textureGetter);

		var model = new LBGBakedModel(Objects.requireNonNull(this.baseModel.bake(baker, textureGetter, rotationContainer, modelId)), this.metadata);

		this.metadata.propagate(model);

		return model;
	}
}
