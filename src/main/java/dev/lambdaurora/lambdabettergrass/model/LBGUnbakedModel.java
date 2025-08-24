/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.model;

import dev.lambdaurora.lambdabettergrass.metadata.LBGMetadata;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents the LambdaBetterGrass unbaked model.
 *
 * @author LambdAurora
 * @version 2.1.0
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
	public void resolveDependencies(Resolver resolver) {
		this.baseModel.resolveDependencies(resolver);
	}

	@Override
	public @NotNull BakedModel bake(
			ModelBaker baker, Function<Material, TextureAtlasSprite> textureGetter,
			ModelState modelState
	) {
		this.metadata.bakeTextures(textureGetter);

		var model = new LBGBakedModel(Objects.requireNonNull(this.baseModel.bake(baker, textureGetter, modelState)), this.metadata);

		this.metadata.propagate(model);

		return model;
	}
}
