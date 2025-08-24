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
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperUnbakedGroupedBlockStateModel;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents the LambdaBetterGrass unbaked model for layer method.
 *
 * @author LambdAurora
 * @version 2.2.0
 * @since 1.0.0
 */
public class LBGLayerUnbakedModel extends WrapperUnbakedGroupedBlockStateModel {
	private final List<LBGCompiledLayerMetadata> metadatas;

	public LBGLayerUnbakedModel(BlockStateModel.UnbakedRoot wrapped, List<LBGCompiledLayerMetadata> metadatas) {
		super(wrapped);
		this.metadatas = metadatas;
	}

	@Override
	public @NotNull Object visualEqualityGroup(BlockState state) {
		this.metadatas.forEach(metadata -> metadata.visualEqualityGroup(state));
		return super.visualEqualityGroup(state);
	}

	@Override
	public void resolveDependencies(Resolver resolver) {
		super.resolveDependencies(resolver);
		this.metadatas.forEach(metadata -> metadata.resolveDependencies(resolver));
	}

	@Override
	public @NotNull BlockStateModel bake(BlockState state, ModelBaker modelBaker) {
		this.metadatas.forEach(metadata -> metadata.bake(state, modelBaker));
		return new LBGLayerBakedModel(super.bake(state, modelBaker), this.metadatas);
	}
}
