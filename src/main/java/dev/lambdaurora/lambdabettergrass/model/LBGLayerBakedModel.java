/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.model;

import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.metadata.layer.LBGCompiledLayerMetadata;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Predicate;

/**
 * Represents the LambdaBetterGrass baked model for layer method.
 *
 * @author LambdAurora
 * @version 2.7.0
 * @since 1.0.0
 */
public class LBGLayerBakedModel extends WrapperBlockStateModel {
	private final List<LBGCompiledLayerMetadata> metadatas;

	public LBGLayerBakedModel(BlockStateModel baseModel, List<LBGCompiledLayerMetadata> metadatas) {
		super(baseModel);
		this.metadatas = metadatas;
	}

	@Override
	public void emitQuads(
			QuadEmitter quadEmitter,
			BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random,
			Predicate<@Nullable Direction> cullTest
	) {
		if (!LambdaBetterGrass.get().hasBetterLayer()) {
			// Don't touch the model.
			super.emitQuads(quadEmitter, world, pos, state, random, cullTest);
			return;
		}

		for (var metadata : this.metadatas) {
			int success = metadata.emitBlockQuads(quadEmitter, world, state, pos, random, cullTest);
			if (success != 0) {
				if (success == 1) {
					final Vector3f offset = metadata.offset();
					if (offset != null) {
						quadEmitter.pushTransform(quad -> {
							Vector3f vec = null;
							for (int i = 0; i < 4; i++) {
								vec = quad.copyPos(i, vec);
								vec.add(offset);
								quad.pos(i, vec);
							}
							return true;
						});
					}
					super.emitQuads(quadEmitter, world, pos, state, random, cullTest);
					if (offset != null) {
						quadEmitter.popTransform();
					}
				}
				return;
			}
		}

		super.emitQuads(quadEmitter, world, pos, state, random, cullTest);
	}
}
