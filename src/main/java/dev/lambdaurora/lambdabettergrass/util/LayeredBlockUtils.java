/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.util;

import dev.lambdaurora.lambdabettergrass.metadata.layer.LBGLayerMetadata;
import dev.lambdaurora.lambdabettergrass.metadata.layer.LBGLayerState;
import dev.lambdaurora.lambdabettergrass.metadata.layer.LBGLayerType;
import dev.lambdaurora.lambdabettergrass.resource.LBGContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;

/**
 * Represents utilities about snow.
 *
 * @author LambdAurora
 * @version 2.5.0
 * @since 1.0.0
 */
public final class LayeredBlockUtils {
	public static final List<Direction> HORIZONTAL_DIRECTIONS = Arrays.stream(Direction.values())
			.filter(dir -> dir.getAxis().isHorizontal()).toList();

	private LayeredBlockUtils() {
		throw new UnsupportedOperationException("LayeredBlockUtils only contains static definitions.");
	}

	public static boolean shouldGrassBeSnowy(
			BlockAndTintGetter world, BlockPos pos, BlockState upState,
			boolean onlyPureSnow, LBGContext context
	) {
		// Ignore blocks that are not rendered through the normal system.
		if (upState.getRenderShape() != RenderShape.MODEL)
			return false;

		var snowLayerType = context.layerTypeManager().get(LBGLayerType.SNOW_LAYER_TYPE);
		if (snowLayerType.isEmpty())
			return false;

		var state = context.getState(upState.getBlock());
		if (!(state instanceof LBGLayerState layerState))
			return false;

		return layerState.streamMetadata(upState)
				.filter(metadata -> metadata.layerType().id.equals(LBGLayerType.SNOW_LAYER_TYPE) && metadata.hasLayerModel())
				.map(LBGLayerMetadata::layerType)
				.findFirst()
				.map(layerType ->
						layerType.getNearbyLayeredBlocks(world, pos.above(), upState.getBlock(), onlyPureSnow) > 1
				)
				.orElse(false);
	}
}
