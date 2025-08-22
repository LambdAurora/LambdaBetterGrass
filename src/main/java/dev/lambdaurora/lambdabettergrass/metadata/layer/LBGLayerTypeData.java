/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata.layer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Represents the metadata of a layer type.
 *
 * @param state the block state associated with this layer type
 */
public record LBGLayerTypeData(BlockState state) {
	public static final Codec<LBGLayerTypeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockState.CODEC.fieldOf("block").forGetter(LBGLayerTypeData::state)
	).apply(instance, LBGLayerTypeData::new));
}
