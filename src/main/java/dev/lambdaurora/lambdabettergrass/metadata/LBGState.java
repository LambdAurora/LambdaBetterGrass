/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.io.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Represents LambdaBetterGrass model states.
 *
 * @author LambdAurora
 * @version 2.2.0
 * @since 1.0.0
 */
public abstract class LBGState {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final String PATH_PREFIX = "bettergrass/states";
	private static final Object2ObjectMap<String, LBGStateProvider> LBG_STATES_TYPE = new Object2ObjectOpenHashMap<>();
	private static final Reference2ObjectMap<Block, LBGState> LBG_STATES = new Reference2ObjectOpenHashMap<>();

	private final Identifier id;
	private final Block block;

	public LBGState(Identifier id, Block block) {
		this.id = id;
		this.block = block;
		putState(block, this);
	}

	/**
	 * {@return the identifier of this state}
	 */
	public @NotNull Identifier id() {
		return this.id;
	}

	/**
	 * {@return the block associated with this state}
	 */
	public @NotNull Block block() {
		return this.block;
	}

	/**
	 * Returns whether the data variant can be applied to the model variant.
	 *
	 * @param modelVariant the model variant
	 * @param dataVariant the data variant
	 * @return {@code true} if the data variant can be applied to the model variant, else {@code false}
	 */
	protected boolean matchVariant(String[] modelVariant, String[] dataVariant) {
		for (String dataProperty : dataVariant) {
			if (dataProperty.equals("*"))
				return true;

			boolean matched = false;
			for (String modelProperty : modelVariant) {
				if (modelProperty.equals(dataProperty)) {
					matched = true;
					break;
				}
			}

			if (!matched)
				return false;
		}

		return true;
	}

	public abstract @Nullable BlockStateModel.UnbakedRoot getCustomUnbakedModel(
			BlockState state, BlockStateModel.UnbakedRoot originalModel
	);

	protected static void putState(Block block, LBGState state) {
		LBG_STATES.put(block, state);
	}

	/**
	 * Returns the state from the cache using its identifier.
	 *
	 * @param block the block of the state
	 * @return the state if cached, else {@code null}
	 */
	public static @Nullable LBGState getMetadataState(Block block) {
		return LBG_STATES.get(block);
	}

	/**
	 * Resets all the known states cache.
	 */
	public static void reset() {
		LBG_STATES.clear();
	}

	public static void registerType(String type, LBGStateProvider stateProvider) {
		LBG_STATES_TYPE.put(type, stateProvider);
	}

	public static void loadMetadataState(
			Identifier id, ResourceManager resourceManager, JsonObject json,
			StateDefinition<Block, BlockState> stateDefinition
	) {
		String type = "grass";
		if (json.has("type"))
			type = json.get("type").getAsString();

		if (LBG_STATES_TYPE.containsKey(type))
			LBG_STATES_TYPE.get(type).create(id, resourceManager, json, stateDefinition);
		else
			LOGGER.warn("Could not find type {} for metadata state {}.", type, id);
	}

	@FunctionalInterface
	public interface LBGStateProvider {
		LBGState create(
				Identifier id, ResourceManager resourceManager, JsonObject json,
				StateDefinition<Block, BlockState> stateDefinition
		);
	}
}
