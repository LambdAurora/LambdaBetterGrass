/*
 * Copyright © 2021, 2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.util;

import dev.lambdaurora.lambdabettergrass.metadata.LBGLayerState;
import dev.lambdaurora.lambdabettergrass.metadata.LBGState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents utilities about snow.
 *
 * @author LambdAurora
 * @version 1.2.2
 * @since 1.0.0
 */
public final class LayeredBlockUtils {
	private static final List<Direction> HORIZONTAL_DIRECTIONS = Arrays.stream(Direction.values())
			.filter(dir -> dir.getAxis().isHorizontal())
			.collect(Collectors.toList());

	private LayeredBlockUtils() {
		throw new UnsupportedOperationException("LayeredBlockUtils only contains static definitions.");
	}

	public static boolean shouldGrassBeSnowy(BlockRenderView world, BlockPos pos, Identifier stateId, BlockState upState, boolean onlyPureSnow) {
		// Ignore blocks that are not rendered through the normal system.
		if (upState.getRenderType() != BlockRenderType.MODEL)
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

		return shouldTry[0] && getNearbySnowyBlocks(world, pos.up(), upState.getBlock(), onlyPureSnow) > 1;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> String nameValue(Property<T> property, Comparable<?> value) {
		return property.name((T) value);
	}

	public static int getNearbySnowyBlocks(BlockRenderView world, BlockPos pos, Block type, boolean onlyPureSnow) {
		return getNearbyLayeredBlocks(world, pos, Blocks.SNOW, type, onlyPureSnow);
	}

	public static int getNearbyLayeredBlocks(BlockRenderView world, BlockPos pos, Block layerBlock, Block type, boolean onlySourceBlock) {
		int nearbySnow = 0;
		for (var direction : HORIZONTAL_DIRECTIONS) {
			var offsetPos = pos.offset(direction);
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

	public static int getNearbyBlockLayers(BlockRenderView world, BlockPos pos, Block layerBlock) {
		int nearbySnow = 0;
		for (var direction : HORIZONTAL_DIRECTIONS) {
			if (world.getBlockState(pos.offset(direction)).getBlock() == layerBlock)
				nearbySnow++;
		}
		return nearbySnow;
	}
}
