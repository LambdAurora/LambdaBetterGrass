/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.model;

import dev.lambdaurora.lambdabettergrass.LBGMode;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.metadata.LBGMetadata;
import dev.lambdaurora.lambdabettergrass.metadata.grass.LBGGrassLayer;
import dev.lambdaurora.lambdabettergrass.util.LayeredBlockUtils;
import it.unimi.dsi.fastutil.ints.Int2BooleanFunction;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Represents the LambdaBetterGrass baked model.
 *
 * @author LambdAurora
 * @version 2.5.0
 * @since 1.0.0
 */
public class LBGBakedModel extends WrapperBlockStateModel {
	private final LBGMetadata metadata;

	public LBGBakedModel(BlockStateModel baseModel, LBGMetadata metadata) {
		super(baseModel);
		this.metadata = metadata;
	}

	@Override
	public void emitQuads(
			QuadEmitter quadEmitter,
			BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random,
			Predicate<@Nullable Direction> cullTest
	) {
		var mode = LambdaBetterGrass.get().config.getMode();

		if (mode == LBGMode.OFF) {
			// Don't touch the model.
			super.emitQuads(quadEmitter, world, pos, state, random, cullTest);
			return;
		}

		if (this.metadata.getSnowyModelVariant() != null && LambdaBetterGrass.get().hasBetterLayer()
				&& state.getProperties().contains(BlockStateProperties.SNOWY) && !state.get(BlockStateProperties.SNOWY)) {
			var upPos = pos.above();
			var up = world.getBlockState(upPos);
			if (!up.isAir()) {
				if (LayeredBlockUtils.shouldGrassBeSnowy(world, pos, up, false, this.metadata.context())) {
					this.metadata.getSnowyModelVariant()
							.emitQuads(
									quadEmitter, world, pos, state.with(BlockStateProperties.SNOWY, true), random, cullTest
							);
					return;
				}
			}
		}

		quadEmitter.pushTransform(quad -> {
			if (canEditQuad(quad)) {
				this.metadata.getLayer(quad.tintIndex()).ifPresent(layer -> {
					if (mode == LBGMode.FASTEST) {
						spriteBake(quad, layer, "connect");
						return;
					}

					Direction face = quad.nominalFace();
					if (face == null)
						return;

					var right = face.getClockWise();
					var left = face.getCounterClockWise();

					if (canFullyConnect(world, state, pos, face)) {
						if (spriteBake(quad, layer, "connect"))
							return;
					}

					if (mode != LBGMode.FANCY)
						return;

					boolean rightMatch = canConnect(world, state, pos.below(), right)
							|| (canConnect(world, state, pos, right) && canFullyConnect(world, state, pos.relative(right), face));
					boolean leftMatch = canConnect(world, state, pos.below(), left)
							|| (canConnect(world, state, pos, left) && canFullyConnect(world, state, pos.relative(left), face));

					if (rightMatch && leftMatch)
						spriteBake(quad, layer, "arch");
					else if (rightMatch)
						spriteBake(quad, layer, "blend_up_m");
					else if (leftMatch)
						spriteBake(quad, layer, "blend_up");
				});
			}
			return true;
		});
		super.emitQuads(quadEmitter, world, pos, state, random, cullTest);
		quadEmitter.popTransform();
	}

	private static boolean canEditQuad(QuadView quad) {
		var nominalFace = quad.nominalFace();

		if (nominalFace == null) return false;
		else if (nominalFace.getAxis() == Direction.Axis.Y) return false;
		else if (testAll(i -> quad.y(i) > 1.f || quad.y(i) < 0.f)) return false;

		if (nominalFace.getAxis() == Direction.Axis.X) {
			return !testAll(i -> quad.z(i) != 0) || !testAll(i -> quad.z(i) != 1);
		} else if (nominalFace.getAxis() == Direction.Axis.Z) {
			return !testAll(i -> quad.x(i) != 0) || !testAll(i -> quad.x(i) != 1);
		}

		return true;
	}

	private static boolean testAll(Int2BooleanFunction tester) {
		for (int i = 0; i < 4; i++) {
			if (!tester.get(i)) return false;
		}

		return true;
	}

	private boolean canFullyConnect(
			BlockAndTintGetter world, BlockState self, BlockPos selfPos, Direction direction
	) {
		return this.canConnect(world, self, selfPos, selfPos.relative(direction).below());
	}

	private boolean canConnect(
			BlockAndTintGetter world, BlockState self, BlockPos start, Direction direction
	) {
		return this.canConnect(world, self, start, start.relative(direction));
	}

	private boolean canConnect(
			BlockAndTintGetter world, BlockState self, BlockPos selfPos, BlockPos adjacentPos
	) {
		var adjacent = world.getBlockState(adjacentPos);
		var upPos = adjacentPos.above();
		var up = world.getBlockState(upPos);

		if (LambdaBetterGrass.get().hasBetterLayer() &&
				self.getBlock() instanceof SnowyDirtBlock) {
			boolean selfSnowy = self.get(BlockStateProperties.SNOWY);

			if (selfSnowy) {
				if (!up.isAir()) {
					if (up.is(Blocks.SNOW))
						return true;
					else if (adjacent.getBlock() instanceof SnowyDirtBlock) {
						if (LayeredBlockUtils.shouldGrassBeSnowy(world, upPos, up, true, this.metadata.context()))
							return true;
					}
				}
			}
		}

		return canConnect(self, adjacent) && (up.isAir() || !up.isFaceSturdy(world, upPos, Direction.DOWN));
	}

	private static boolean canConnect(BlockState self, BlockState adjacent) {
		return self == adjacent;
	}

	private static boolean spriteBake(MutableQuadView quad, LBGGrassLayer layer, String texture) {
		var sprite = layer.getBakedTexture(texture);
		if (sprite != null)
			quad.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV);
		return sprite != null;
	}
}
