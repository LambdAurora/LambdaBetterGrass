/*
 * Copyright © 2021, 2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.model;

import com.mojang.datafixers.util.Pair;
import dev.lambdaurora.lambdabettergrass.metadata.LBGMetadata;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Represents the LambdaBetterGrass unbaked model.
 *
 * @author LambdAurora
 * @version 1.2.1
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
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter,
	                                                           Set<Pair<String, String>> unresolvedTextureReferences) {
		var baseIds = this.baseModel.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences);
		var textures = new ArrayList<>(baseIds);
		textures.addAll(this.metadata.getTextures());
		if (this.metadata.getSnowyVariant() != null)
			textures.addAll(this.metadata.getSnowyVariant().getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences));
		return textures;
	}

	@Override
	public @Nullable BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer,
	                                 Identifier modelId) {
		this.metadata.bakeTextures(textureGetter);

		var model = new LBGBakedModel(Objects.requireNonNull(this.baseModel.bake(loader, textureGetter, rotationContainer, modelId)), this.metadata);

		this.metadata.propagate(model);

		return model;
	}
}
