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
import net.fabricmc.fabric.api.client.model.loading.v1.WrapperGroupableModel;
import net.minecraft.client.renderer.block.model.UnbakedBlockStateModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents the LambdaBetterGrass unbaked model.
 *
 * @author LambdAurora
 * @version 2.1.0
 * @since 1.0.0
 */
public class LBGUnbakedModel extends WrapperGroupableModel implements UnbakedBlockStateModel {
	private final UnbakedBlockStateModel wrapped;
	private final LBGMetadata metadata;

	public LBGUnbakedModel(UnbakedBlockStateModel wrapped, LBGMetadata metadata) {
		super(wrapped);
		this.wrapped = wrapped;
		this.metadata = metadata;
	}

	@Override
	public @NotNull Object visualEqualityGroup(BlockState state) {
		return this.wrapped.visualEqualityGroup(state);
	}

	@Override
	public void resolveDependencies(Resolver resolver) {
		this.wrapped.resolveDependencies(resolver);
	}

	@Override
	public @NotNull BakedModel bake(ModelBaker baker) {
		this.metadata.bakeTextures(baker.sprites());

		var model = new LBGBakedModel(Objects.requireNonNull(this.wrapped.bake(baker)), this.metadata);

		this.metadata.propagate(model);

		return model;
	}
}
