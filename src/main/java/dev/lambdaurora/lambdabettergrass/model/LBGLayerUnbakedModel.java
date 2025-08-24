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
import net.fabricmc.fabric.api.client.model.loading.v1.WrapperGroupableModel;
import net.minecraft.client.renderer.block.model.UnbakedBlockStateModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Represents the LambdaBetterGrass unbaked model for layer method.
 *
 * @author LambdAurora
 * @version 2.1.0
 * @since 1.0.0
 */
public class LBGLayerUnbakedModel extends WrapperGroupableModel implements UnbakedBlockStateModel {
	private final UnbakedBlockStateModel wrapped;
	private final List<LBGCompiledLayerMetadata> metadatas;

	public LBGLayerUnbakedModel(UnbakedBlockStateModel wrapped, List<LBGCompiledLayerMetadata> metadatas) {
		super(wrapped);
		this.wrapped = wrapped;
		this.metadatas = metadatas;
	}

	@Override
	public @NotNull Object visualEqualityGroup(BlockState state) {
		this.metadatas.forEach(metadata -> metadata.visualEqualityGroup(state));
		return this.wrapped.visualEqualityGroup(state);
	}

	@Override
	public void resolveDependencies(Resolver resolver) {
		this.wrapped.resolveDependencies(resolver);
		this.metadatas.forEach(metadata -> metadata.resolveDependencies(resolver));
	}

	@Override
	public @NotNull BakedModel bake(ModelBaker modelBaker) {
		this.metadatas.forEach(metadata -> metadata.bake(modelBaker));
		return new LBGLayerBakedModel(Objects.requireNonNull(this.wrapped.bake(modelBaker)), this.metadatas);
	}
}
