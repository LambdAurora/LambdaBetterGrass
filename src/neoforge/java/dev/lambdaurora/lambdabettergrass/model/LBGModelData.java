/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.model;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public record LBGModelData(BlockAndTintGetter world, BlockPos pos) {
	public static final ModelProperty<LBGModelData> PROPERTY = new ModelProperty<>(data -> data != null);

	public LBGModelData(BlockAndTintGetter world, BlockPos pos) {
		this.world = world;
		this.pos = pos.immutable();
	}
}
