/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata.layer;

import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.util.LayeredBlockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;

/**
 * Represents a layer type.
 *
 * @author LambdAurora
 * @version 2.2.0
 * @since 1.0.0
 */
public final class LBGLayerType {
	public static final Identifier SNOW_LAYER_TYPE = LambdaBetterGrass.id("snow");
	public static final Identifier LEAF_LITTER_LAYER_TYPE = LambdaBetterGrass.id("leaf_litter");

	public final Identifier id;
	public final LBGLayerTypeData data;
	public final RenderType renderType;

	public LBGLayerType(Identifier id, LBGLayerTypeData data) {
		this.id = id;
		this.data = data;
		this.renderType = ItemBlockRenderTypes.getChunkRenderType(data.state());
	}

	public int getNearbyLayeredBlocks(BlockAndTintGetter world, BlockPos pos, Block type, boolean onlySourceBlock) {
		int nearbyLayer = 0;
		for (var direction : LayeredBlockUtils.HORIZONTAL_DIRECTIONS) {
			var offsetPos = pos.relative(direction);
			var state = world.getBlockState(offsetPos);
			if (state.is(type) && !onlySourceBlock) {
				if (this.getNearbyBlockLayers(world, offsetPos) > 1)
					nearbyLayer++;
			} else if (this.data.match(state)) {
				nearbyLayer++;
			}
		}
		return nearbyLayer;
	}

	public int getNearbyBlockLayers(BlockAndTintGetter world, BlockPos pos) {
		int nearbyLayer = 0;
		for (var direction : LayeredBlockUtils.HORIZONTAL_DIRECTIONS) {
			if (this.data.match(world.getBlockState(pos.relative(direction))))
				nearbyLayer++;
		}
		return nearbyLayer;
	}

	/**
	 * {@return the baked layer model}
	 */
	public BlockStateModel getLayerModel() {
		return Minecraft.getInstance().getBlockRenderer().getBlockModel(this.data.state());
	}

	@Override
	public String toString() {
		return "LBGLayerType{" +
				"id=" + this.id +
				", data=" + this.data +
				'}';
	}
}
