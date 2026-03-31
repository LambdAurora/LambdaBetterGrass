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
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperUnbakedRootBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Represents the LambdaBetterGrass unbaked model.
 *
 * @author LambdAurora
 * @version 2.2.0
 * @since 1.0.0
 */
public class LBGUnbakedModel extends WrapperUnbakedRootBlockStateModel {
	private final LBGMetadata metadata;

	public LBGUnbakedModel(BlockStateModel.UnbakedRoot wrapped, LBGMetadata metadata) {
		super(wrapped);
		this.metadata = metadata;
	}

	@Override
	public BlockStateModel bake(BlockState state, ModelBaker baker) {
		this.metadata.bakeMaterials(baker.materials());

		var model = new LBGBakedModel(super.bake(state, baker), this.metadata);

		this.metadata.propagate(model);

		return model;
	}
}
