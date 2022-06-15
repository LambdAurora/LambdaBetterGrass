/*
 * Copyright © 2021-2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.model;

import dev.lambdaurora.lambdabettergrass.LBGMode;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.metadata.LBGLayer;
import dev.lambdaurora.lambdabettergrass.metadata.LBGMetadata;
import dev.lambdaurora.lambdabettergrass.util.LayeredBlockUtils;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowyBlock;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;

import java.util.function.Supplier;

/**
 * Represents the LambdaBetterGrass baked model.
 *
 * @author LambdAurora
 * @version 1.2.4
 * @since 1.0.0
 */
public class LBGBakedModel extends ForwardingBakedModel {
	private final LBGMetadata metadata;

	public LBGBakedModel(BakedModel baseModel, LBGMetadata metadata) {
		this.wrapped = baseModel;
		this.metadata = metadata;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockRenderView world, BlockState state, BlockPos pos, Supplier<RandomGenerator> randomSupplier, RenderContext context) {
		var mode = LambdaBetterGrass.get().config.getMode();

		if (mode == LBGMode.OFF) {
			// Don't touch the model.
			super.emitBlockQuads(world, state, pos, randomSupplier, context);
			return;
		}

		if (this.metadata.getSnowyModelVariant() != null && LambdaBetterGrass.get().hasBetterLayer()
				&& state.getProperties().contains(Properties.SNOWY) && !state.get(Properties.SNOWY)) {
			var upPos = pos.up();
			var up = world.getBlockState(upPos);
			if (!up.isAir()) {
				var blockId = Registry.BLOCK.getId(up.getBlock());
				var stateId = new Identifier(blockId.getNamespace(), "bettergrass/states/" + blockId.getPath());
				if (LayeredBlockUtils.shouldGrassBeSnowy(world, pos, stateId, up, false)) {
					((FabricBakedModel) this.metadata.getSnowyModelVariant())
							.emitBlockQuads(world, state.with(Properties.SNOWY, true), pos, randomSupplier, context);
					return;
				}
			}
		}

		context.pushTransform(quad -> {
			if (quad.nominalFace().getAxis() != Direction.Axis.Y) {
				this.metadata.getLayer(quad.colorIndex()).ifPresent(layer -> {
					if (mode == LBGMode.FASTEST) {
						spriteBake(quad, layer, "connect");
						return;
					}

					Direction face = quad.nominalFace();
					var right = face.rotateYClockwise();
					var left = face.rotateYCounterclockwise();

					if (canFullyConnect(world, state, pos, face)) {
						if (spriteBake(quad, layer, "connect"))
							return;
					}

					if (mode != LBGMode.FANCY)
						return;

					boolean rightMatch = canConnect(world, state, pos.down(), right)
							|| (canConnect(world, state, pos, right) && canFullyConnect(world, state, pos.offset(right), face));
					boolean leftMatch = canConnect(world, state, pos.down(), left)
							|| (canConnect(world, state, pos, left) && canFullyConnect(world, state, pos.offset(left), face));

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
		super.emitBlockQuads(world, state, pos, randomSupplier, context);
		context.popTransform();
	}

	private static boolean canFullyConnect(BlockRenderView world, BlockState self, BlockPos selfPos, Direction direction) {
		return canConnect(world, self, selfPos, selfPos.offset(direction).down());
	}

	private static boolean canConnect(BlockRenderView world, BlockState self, BlockPos start, Direction direction) {
		return canConnect(world, self, start, start.offset(direction));
	}

	private static boolean canConnect(BlockRenderView world, BlockState self, BlockPos selfPos, BlockPos adjacentPos) {
		var adjacent = world.getBlockState(adjacentPos);
		var upPos = adjacentPos.up();
		var up = world.getBlockState(upPos);

		if (LambdaBetterGrass.get().hasBetterLayer() &&
				self.getBlock() instanceof SnowyBlock) {
			boolean selfSnowy = self.get(Properties.SNOWY);

			if (selfSnowy) {
				if (!up.isAir()) {
					if (up.isOf(Blocks.SNOW))
						return true;
					else if (adjacent.getBlock() instanceof SnowyBlock) {
						var blockId = Registry.BLOCK.getId(up.getBlock());
						var stateId = new Identifier(blockId.getNamespace(), "bettergrass/states/" + blockId.getPath());
						if (LayeredBlockUtils.shouldGrassBeSnowy(world, adjacentPos, stateId, up, true))
							return true;
					}
				}
			}
		}

		return canConnect(self, adjacent) && (up.isAir() || !up.isSideSolidFullSquare(world, upPos, Direction.DOWN));
	}

	private static boolean canConnect(BlockState self, BlockState adjacent) {
		return self == adjacent;
	}

	private static boolean spriteBake(MutableQuadView quad, LBGLayer layer, String texture) {
		var sprite = layer.getBakedTexture(texture);
		if (sprite != null)
			quad.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
		return sprite != null;
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomGenerator> randomSupplier, RenderContext context) {
		throw new UnsupportedOperationException("LambdaBetterGrass models should never try to render as an item!");
	}
}
