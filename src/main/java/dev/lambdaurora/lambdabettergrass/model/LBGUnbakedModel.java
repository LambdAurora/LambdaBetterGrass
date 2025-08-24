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
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperUnbakedGroupedBlockStateModel;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the LambdaBetterGrass unbaked model.
 *
 * @author LambdAurora
 * @version 2.2.0
 * @since 1.0.0
 */
public class LBGUnbakedModel extends WrapperUnbakedGroupedBlockStateModel {
	private final LBGMetadata metadata;

	public LBGUnbakedModel(BlockStateModel.UnbakedRoot wrapped, LBGMetadata metadata) {
		super(wrapped);
		this.metadata = metadata;
	}

	@Override
	public @NotNull BlockStateModel bake(BlockState state, ModelBaker baker) {
		this.metadata.bakeTextures(baker.sprites());

		var model = new LBGBakedModel(super.bake(state, baker), this.metadata);

		this.metadata.propagate(model);

		return model;
	}
}
