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
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.resources.model.ModelIdentifier;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.io.ResourceManager;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.function.Function;

/**
 * Represents LambdaBetterGrass model states.
 *
 * @author LambdAurora
 * @version 1.4.0
 * @since 1.0.0
 */
public abstract class LBGState {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final String PATH_PREFIX = "bettergrass/states";
	private static final Object2ObjectMap<String, LBGStateProvider> LBG_STATES_TYPE = new Object2ObjectOpenHashMap<>();
	private static final Object2ObjectMap<Identifier, LBGState> LBG_STATES = new Object2ObjectOpenHashMap<>();

	public final Identifier id;

	public LBGState(Identifier id) {
		this.id = id;
		putState(id, this);
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

	public abstract @Nullable UnbakedModel getCustomUnbakedModel(ModelIdentifier modelId, UnbakedModel originalModel,
			Function<Identifier, UnbakedModel> modelGetter);

	protected static void putState(Identifier id, LBGState state) {
		LBG_STATES.put(id, state);
	}

	/**
	 * Returns the state from the cache using its identifier.
	 *
	 * @param id the identifier of the state
	 * @return the state if cached, else {@code null}
	 */
	public static @Nullable LBGState getMetadataState(Identifier id) {
		return LBG_STATES.get(id);
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

	public static void loadMetadataState(Identifier id, Block block, ResourceManager resourceManager, JsonObject json,
			BlockModelDefinition.Context deserializationContext) {
		String type = "grass";
		if (json.has("type"))
			type = json.get("type").getAsString();

		if (LBG_STATES_TYPE.containsKey(type))
			LBG_STATES_TYPE.get(type).create(id, block, resourceManager, json, deserializationContext);
		else
			LOGGER.warn("Could not find type {} for metadata state {}.", type, id);
	}

	@FunctionalInterface
	public interface LBGStateProvider {
		LBGState create(Identifier id, Block block, ResourceManager resourceManager, JsonObject json,
				BlockModelDefinition.Context deserializationContext);
	}
}
