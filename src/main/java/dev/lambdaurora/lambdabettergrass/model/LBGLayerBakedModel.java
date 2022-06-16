/*
 * Copyright © 2021-2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.model;

import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.metadata.LBGCompiledLayerMetadata;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.BlockRenderView;

import java.util.List;
import java.util.function.Supplier;

/**
 * Represents the LambdaBetterGrass baked model for layer method.
 *
 * @author LambdAurora
 * @version 1.3.0
 * @since 1.0.0
 */
public class LBGLayerBakedModel extends ForwardingBakedModel {
	private final List<LBGCompiledLayerMetadata> metadatas;

	public LBGLayerBakedModel(BakedModel baseModel, List<LBGCompiledLayerMetadata> metadatas) {
		this.wrapped = baseModel;
		this.metadatas = metadatas;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockRenderView world, BlockState state, BlockPos pos, Supplier<RandomGenerator> randomSupplier, RenderContext context) {
		if (!LambdaBetterGrass.get().hasBetterLayer()) {
			// Don't touch the model.
			super.emitBlockQuads(world, state, pos, randomSupplier, context);
			return;
		}

		for (var metadata : this.metadatas) {
			int success = metadata.emitBlockQuads(world, state, pos, randomSupplier, context);
			if (success != 0) {
				if (success == 1) {
					final Vec3f offset = metadata.offset();
					if (offset != null) {
						context.pushTransform(quad -> {
							Vec3f vec = null;
							for (int i = 0; i < 4; i++) {
								vec = quad.copyPos(i, vec);
								vec.add(offset);
								quad.pos(i, vec);
							}
							return true;
						});
					}
					super.emitBlockQuads(world, state, pos, randomSupplier, context);
					if (offset != null) {
						context.popTransform();
					}
				}
				return;
			}
		}

		super.emitBlockQuads(world, state, pos, randomSupplier, context);
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomGenerator> randomSupplier, RenderContext context) {
		throw new UnsupportedOperationException("LambdaBetterGrass models should never try to render as an item!");
	}
}
