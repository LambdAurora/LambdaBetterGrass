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
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.DelegateBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents the LambdaBetterGrass baked model for layer method.
 *
 * @author LambdAurora
 * @version 2.1.0
 * @since 1.0.0
 */
public class LBGLayerBakedModel extends DelegateBakedModel {
	private final List<LBGCompiledLayerMetadata> metadatas;

	public LBGLayerBakedModel(BakedModel baseModel, List<LBGCompiledLayerMetadata> metadatas) {
		super(baseModel);
		this.metadatas = metadatas;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(
			QuadEmitter quadEmitter,
			BlockAndTintGetter world, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier,
			Predicate<@Nullable Direction> cullTest
	) {
		if (!LambdaBetterGrass.get().hasBetterLayer()) {
			// Don't touch the model.
			super.emitBlockQuads(quadEmitter, world, state, pos, randomSupplier, cullTest);
			return;
		}

		for (var metadata : this.metadatas) {
			int success = metadata.emitBlockQuads(quadEmitter, world, state, pos, randomSupplier, cullTest);
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
					super.emitBlockQuads(quadEmitter, world, state, pos, randomSupplier, cullTest);
					if (offset != null) {
						quadEmitter.popTransform();
					}
				}
				return;
			}
		}

		super.emitBlockQuads(quadEmitter, world, state, pos, randomSupplier, cullTest);
	}
}
