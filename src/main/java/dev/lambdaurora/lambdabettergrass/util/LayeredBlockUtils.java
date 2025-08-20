/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.util;

import dev.lambdaurora.lambdabettergrass.metadata.LBGLayerState;
import dev.lambdaurora.lambdabettergrass.metadata.LBGState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Arrays;
import java.util.List;

/**
 * Represents utilities about snow.
 *
 * @author LambdAurora
 * @version 1.4.0
 * @since 1.0.0
 */
public final class LayeredBlockUtils {
	private static final List<Direction> HORIZONTAL_DIRECTIONS = Arrays.stream(Direction.values())
			.filter(dir -> dir.getAxis().isHorizontal()).toList();

	private LayeredBlockUtils() {
		throw new UnsupportedOperationException("LayeredBlockUtils only contains static definitions.");
	}

	public static boolean shouldGrassBeSnowy(BlockAndTintGetter world, BlockPos pos, Identifier stateId, BlockState upState,
			boolean onlyPureSnow) {
		// Ignore blocks that are not rendered through the normal system.
		if (upState.getRenderShape() != RenderShape.MODEL)
			return false;

		var state = LBGState.getMetadataState(stateId);
		if (!(state instanceof LBGLayerState layerState))
			return false;

		var properties = upState.getProperties();
		var modelVariant = new String[properties.size()];

		int i = 0;
		for (var property : properties) {
			var end = ",";
			if (modelVariant.length == i + 1)
				end = "";
			modelVariant[i] = property.getName() + '=' + nameValue(property, upState.get(property)) + end;
			i++;
		}

		boolean[] shouldTry = {false};
		layerState.forEach(modelVariant, metadata -> {
			if (metadata.layerType.getName().equals("snow") && metadata.hasLayerModel()) {
				shouldTry[0] = true;
			}
		});

		return shouldTry[0] && getNearbySnowyBlocks(world, pos.above(), upState.getBlock(), onlyPureSnow) > 1;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> String nameValue(Property<T> property, Comparable<?> value) {
		return property.getName((T) value);
	}

	public static int getNearbySnowyBlocks(BlockAndTintGetter world, BlockPos pos, Block type, boolean onlyPureSnow) {
		return getNearbyLayeredBlocks(world, pos, Blocks.SNOW, type, onlyPureSnow);
	}

	public static int getNearbyLayeredBlocks(BlockAndTintGetter world, BlockPos pos, Block layerBlock, Block type, boolean onlySourceBlock) {
		int nearbySnow = 0;
		for (var direction : HORIZONTAL_DIRECTIONS) {
			var offsetPos = pos.relative(direction);
			var block = world.getBlockState(offsetPos).getBlock();
			if (block == type && !onlySourceBlock) {
				if (getNearbyBlockLayers(world, offsetPos, layerBlock) > 1)
					nearbySnow++;
			} else if (block == layerBlock) {
				nearbySnow++;
			}
		}
		return nearbySnow;
	}

	public static int getNearbyBlockLayers(BlockAndTintGetter world, BlockPos pos, Block layerBlock) {
		int nearbySnow = 0;
		for (var direction : HORIZONTAL_DIRECTIONS) {
			if (world.getBlockState(pos.relative(direction)).getBlock() == layerBlock)
				nearbySnow++;
		}
		return nearbySnow;
	}
}
