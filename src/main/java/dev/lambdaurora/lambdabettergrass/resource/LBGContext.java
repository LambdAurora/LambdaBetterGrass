/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.resource;

import dev.lambdaurora.lambdabettergrass.metadata.LBGState;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Represents the context used for LambdaBetterGrass states and models.
 *
 * @author LambdAurora
 * @version 2.5.0
 * @since 2.5.0
 */
public final class LBGContext {
	private final LBGLayerTypeManager layerTypeManager;
	final Map<Block, LBGState> states = new Reference2ObjectOpenHashMap<>();

	public LBGContext(LBGLayerTypeManager layerTypeManager) {
		this.layerTypeManager = layerTypeManager;
	}

	/**
	 * {@return the layer type manager}
	 */
	public LBGLayerTypeManager layerTypeManager() {
		return this.layerTypeManager;
	}

	/**
	 * Gets a LambdaBetterGrass state for a given block.
	 *
	 * @param block the block
	 * @return the LambdaBetterGrass state if one is associated with the given block, or {@code null} otherwise
	 */
	public @Nullable LBGState getState(Block block) {
		return this.states.get(block);
	}
}
