/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata.layer;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a compiled layer metadata.
 * <p>
 * This holds the custom models to use when the layer variation should be used.
 *
 * @author LambdAurora
 * @version 2.1.0
 * @since 1.0.0
 */
public class LBGCompiledLayerMetadata {
	public final LBGLayerType layerType;
	private final boolean hasLayer;
	private final @Nullable Vector3f offset;
	public final LBGLayerMetadata.LayerUnbakedModels unbakedModels;
	private BakedModel bakedAlternateModel;

	public LBGCompiledLayerMetadata(
			LBGLayerType layerType, boolean hasLayer, @Nullable Vector3f offset, LBGLayerMetadata.LayerUnbakedModels unbakedModels
	) {
		this.layerType = layerType;
		this.hasLayer = hasLayer;
		this.offset = offset;
		this.unbakedModels = unbakedModels;
	}

	public @Nullable Vector3f offset() {
		return this.offset;
	}

	public void resolveDependencies(ResolvableModel.Resolver resolver) {
		if (this.unbakedModels.alternateModel() != null) {
			this.unbakedModels.alternateModel().resolveDependencies(resolver);
		}
	}

	public void visualEqualityGroup(BlockState state) {
		if (this.unbakedModels.alternateModel() != null) {
			this.unbakedModels.alternateModel().visualEqualityGroup(state);
		}
	}

	/**
	 * Bakes the hold unbaked models.
	 *
	 * @param baker the model baker
	 */
	public void bake(
			ModelBaker baker
	) {
		if (this.unbakedModels.alternateModel() != null) {
			this.bakedAlternateModel = this.unbakedModels.alternateModel().bake(baker);
		}
	}

	/**
	 * Emits the block quads.
	 *
	 * @param quadEmitter the quad emitter
	 * @param world the world
	 * @param state the block state
	 * @param pos the block position
	 * @param randomSupplier the random supplier
	 * @return {@code 0} if no custom models have emitted quads, {@code 1} if only the layer model has emitted quads,
	 * or {@code 2} if the custom alternative model has emitted quads
	 */
	public int emitBlockQuads(
			QuadEmitter quadEmitter,
			BlockAndTintGetter world, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier,
			Predicate<@Nullable Direction> cullTest
	) {
		int success = 0;
		var layerState = this.layerType.data.state();

		if (this.hasLayer) {
			final var downPos = pos.below();
			final var downState = world.getBlockState(downPos);

			if (downState.isFaceSturdy(world, downPos, Direction.UP)
					&& this.layerType.getNearbyLayeredBlocks(world, pos, state.getBlock(), false) > 1
			) {
				var layerModel = this.layerType.getLayerModel();

				Vec3 offset = state.getOffset(pos);
				boolean pushed = false;

				final var materialFinder = Renderer.get().materialFinder();
				var offsetPos = new BlockPos.Mutable();
				quadEmitter.pushTransform(quad -> {
					var originalMaterial = quad.material();
					var material = materialFinder.copyFrom(originalMaterial)
							.ambientOcclusion(TriState.of(layerModel.useAmbientOcclusion()));

					if (material.blendMode() == BlendMode.DEFAULT) {
						material = material.blendMode(BlendMode.fromRenderLayer(this.layerType.renderType));
					}

					quad.material(material.find());

					var cullFace = quad.cullFace();
					if (cullFace != null && cullFace.getAxis() != Direction.Axis.Y) {
						offsetPos.setWithOffset(pos, cullFace);

						if (Block.shouldRenderFace(layerState, world.getBlockState(offsetPos), cullFace)) {
							quad.cullFace(null);
						} else {
							return false;
						}
					}

					return true;
				});

				if (offset.x != 0.0D || offset.y != 0.0D || offset.z != 0.0D) {
					var offsetVec = new Vector3f((float) offset.x, (float) offset.y, (float) offset.z);
					quadEmitter.pushTransform(quad -> {
						Vector3f vec = null;
						for (int i = 0; i < 4; i++) {
							vec = quad.copyPos(i, vec);
							vec.sub(offsetVec);
							quad.pos(i, vec);
						}
						return true;
					});
					pushed = true;
				}
				layerModel.emitBlockQuads(quadEmitter, world, layerState, pos, randomSupplier, cullTest);
				success = 1;
				if (pushed)
					quadEmitter.popTransform();
				quadEmitter.popTransform();
			}
		}

		if (this.layerType.getNearbyLayeredBlocks(world, pos, state.getBlock(), false) > 1
				&& this.bakedAlternateModel != null) {
			this.bakedAlternateModel.emitBlockQuads(quadEmitter, world, state, pos, randomSupplier, cullTest);
			success = 2;
		}

		return success;
	}
}
