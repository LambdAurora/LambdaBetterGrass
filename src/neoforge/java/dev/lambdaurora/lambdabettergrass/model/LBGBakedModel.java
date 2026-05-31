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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the LambdaBetterGrass baked model on NeoForge.
 *
 * @author LambdAurora
 * @version 2.0.4
 * @since 2.0.4
 */
public class LBGBakedModel extends BakedModelWrapper<BakedModel> implements IDynamicBakedModel {
	private final LBGMetadata metadata;

	public LBGBakedModel(BakedModel baseModel, LBGMetadata metadata) {
		super(baseModel);
		this.metadata = metadata;
	}

	@Override
	public ModelData getModelData(BlockAndTintGetter world, BlockPos pos, BlockState state, ModelData modelData) {
		return super.getModelData(world, pos, state, modelData)
				.derive()
				.with(LBGModelData.PROPERTY, new LBGModelData(world, pos))
				.build();
	}

	@Override
	public List<BakedQuad> getQuads(
			@Nullable BlockState state, @Nullable Direction side, RandomSource random, ModelData extraData,
			@Nullable RenderType renderType
	) {
		if (state == null) {
			return super.getQuads(null, side, random, extraData, renderType);
		}

		var mode = LambdaBetterGrass.get().config.getMode();
		if (mode == LBGMode.OFF) {
			return super.getQuads(state, side, random, extraData, renderType);
		}

		var context = extraData.get(LBGModelData.PROPERTY);
		if (context == null) {
			return super.getQuads(state, side, random, extraData, renderType);
		}

		if (this.metadata.getSnowyModelVariant() != null && LambdaBetterGrass.get().hasBetterLayer()
				&& state.getProperties().contains(BlockStateProperties.SNOWY) && !state.get(BlockStateProperties.SNOWY)) {
			var upPos = context.pos().above();
			var up = context.world().getBlockState(upPos);
			if (!up.isAir()) {
				var blockId = BuiltInRegistries.BLOCK.getId(up.getBlock());
				if (LayeredBlockUtils.shouldGrassBeSnowy(context.world(), context.pos(), blockId, up, false)) {
					return NeoForgeModelHelper.getQuads(
							this.metadata.getSnowyModelVariant(),
							state.with(BlockStateProperties.SNOWY, true),
							side,
							random,
							extraData,
							renderType
					);
				}
			}
		}

		var quads = super.getQuads(state, side, random, extraData, renderType);
		if (quads.isEmpty()) {
			return quads;
		}

		List<BakedQuad> modified = null;
		for (int i = 0; i < quads.size(); i++) {
			var quad = quads.get(i);
			var newQuad = this.transformQuad(quad, mode, context.world(), state, context.pos());

			if (newQuad != quad && modified == null) {
				modified = new ArrayList<>(quads.subList(0, i));
			}

			if (modified != null) {
				modified.add(newQuad);
			}
		}

		return modified == null ? quads : modified;
	}

	private BakedQuad transformQuad(BakedQuad quad, LBGMode mode, BlockAndTintGetter world, BlockState state, BlockPos pos) {
		if (!canEditQuad(quad)) {
			return quad;
		}

		var layer = this.metadata.getLayer(quad.getTintIndex());
		if (layer.isEmpty()) {
			return quad;
		}

		if (mode == LBGMode.FASTEST) {
			return spriteBake(quad, layer.get(), "connect");
		}

		Direction face = quad.getDirection();
		if (face == null) {
			return quad;
		}

		var right = face.getClockWise();
		var left = face.getCounterClockWise();

		if (canFullyConnect(world, state, pos, face)) {
			var baked = spriteBake(quad, layer.get(), "connect");
			if (baked != quad) {
				return baked;
			}
		}

		if (mode != LBGMode.FANCY) {
			return quad;
		}

		boolean rightMatch = canConnect(world, state, pos.below(), right)
				|| (canConnect(world, state, pos, right) && canFullyConnect(world, state, pos.relative(right), face));
		boolean leftMatch = canConnect(world, state, pos.below(), left)
				|| (canConnect(world, state, pos, left) && canFullyConnect(world, state, pos.relative(left), face));

		if (rightMatch && leftMatch) {
			return spriteBake(quad, layer.get(), "arch");
		} else if (rightMatch) {
			return spriteBake(quad, layer.get(), "blend_up_m");
		} else if (leftMatch) {
			return spriteBake(quad, layer.get(), "blend_up");
		}

		return quad;
	}

	private static boolean canEditQuad(BakedQuad quad) {
		var nominalFace = quad.getDirection();

		if (nominalFace == null) return false;
		else if (nominalFace.getAxis() == Direction.Axis.Y) return false;
		else if (testAll(i -> LBGQuadUtils.y(quad, i) > 1.f || LBGQuadUtils.y(quad, i) < 0.f)) return false;

		if (nominalFace.getAxis() == Direction.Axis.X) {
			return !testAll(i -> LBGQuadUtils.z(quad, i) != 0) || !testAll(i -> LBGQuadUtils.z(quad, i) != 1);
		} else if (nominalFace.getAxis() == Direction.Axis.Z) {
			return !testAll(i -> LBGQuadUtils.x(quad, i) != 0) || !testAll(i -> LBGQuadUtils.x(quad, i) != 1);
		}

		return true;
	}

	private static boolean testAll(Int2BooleanFunction tester) {
		for (int i = 0; i < 4; i++) {
			if (!tester.get(i)) return false;
		}

		return true;
	}

	private static boolean canFullyConnect(
			BlockAndTintGetter world, BlockState self, BlockPos selfPos, Direction direction
	) {
		return canConnect(world, self, selfPos, selfPos.relative(direction).below());
	}

	private static boolean canConnect(
			BlockAndTintGetter world, BlockState self, BlockPos start, Direction direction
	) {
		return canConnect(world, self, start, start.relative(direction));
	}

	private static boolean canConnect(
			BlockAndTintGetter world, BlockState self, BlockPos selfPos, BlockPos adjacentPos
	) {
		var adjacent = world.getBlockState(adjacentPos);
		var upPos = adjacentPos.above();
		var up = world.getBlockState(upPos);

		if (LambdaBetterGrass.get().hasBetterLayer() && self.getBlock() instanceof SnowyDirtBlock) {
			boolean selfSnowy = self.get(BlockStateProperties.SNOWY);

			if (selfSnowy && !up.isAir()) {
				if (up.is(Blocks.SNOW)) {
					return true;
				} else if (adjacent.getBlock() instanceof SnowyDirtBlock) {
					var blockId = BuiltInRegistries.BLOCK.getId(up.getBlock());
					if (LayeredBlockUtils.shouldGrassBeSnowy(world, upPos, blockId, up, true)) {
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

	private static BakedQuad spriteBake(BakedQuad quad, LBGGrassLayer layer, String texture) {
		var sprite = layer.getBakedTexture(texture);
		return sprite == null ? quad : LBGQuadUtils.bakeSprite(quad, sprite);
	}
}
