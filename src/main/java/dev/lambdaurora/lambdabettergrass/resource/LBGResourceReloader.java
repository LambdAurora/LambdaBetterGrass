/*
 * Copyright © 2023 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.resource;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import dev.lambdaurora.lambdabettergrass.metadata.LBGLayerType;
import dev.lambdaurora.lambdabettergrass.metadata.LBGState;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Represents the LambdaBetterGrass resource reloader.
 *
 * @author LambdAurora
 * @version 1.4.0
 * @since 1.4.0
 */
public class LBGResourceReloader {
	private static final Logger LOGGER = LogUtils.getLogger();

	public void reload(ResourceManager resourceManager) {
		LBGState.reset();
		LBGLayerType.reset();
		var layerTypes = resourceManager.findResources("bettergrass/layer_types",
				path -> path.getPath().endsWith(".json"));
		layerTypes.forEach(LBGLayerType::load);

		this.loadStates(resourceManager);
	}

	private void loadStates(ResourceManager resourceManager) {
		var variantMapDeserializationContext = new ModelVariantMap.DeserializationContext();

		resourceManager.findResources(LBGState.PATH_PREFIX, id -> id.getPath().endsWith(".json"))
				.forEach((id, resource) -> this.loadState(resourceManager, id, resource, variantMapDeserializationContext));
	}

	/**
	 * Loads the given LambdaBetterGrass state.
	 *
	 * @param resourceManager the resource manager
	 * @param id the resource identifier of the state
	 * @param resource the resource
	 * @param variantMapDeserializationContext the deserialization context of model variants
	 */
	private void loadState(ResourceManager resourceManager, Identifier id, Resource resource,
			ModelVariantMap.DeserializationContext variantMapDeserializationContext) {
		var stateId = new Identifier(
				id.getNamespace(),
				id.getPath().substring(LBGState.PATH_PREFIX.length() + 1, id.getPath().length() - ".json".length())
		);

		var block = Registries.BLOCK.getOrEmpty(stateId);
		if (block.isEmpty()) {
			// The block doesn't exist, so we just ignore the state file.
			return;
		}

		try (var reader = new InputStreamReader(resource.open())) {
			var json = JsonParser.parseReader(reader).getAsJsonObject();
			LBGState.loadMetadataState(stateId, block.get(), resourceManager, json, variantMapDeserializationContext);
		} catch (IOException e) {
			LOGGER.warn("Failed to load LambdaBetterGrass state {}.", stateId, e);
		}
	}
}
