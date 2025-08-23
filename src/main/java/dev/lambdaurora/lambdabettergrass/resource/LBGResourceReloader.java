/*
 * Copyright © 2023 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.resource;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import dev.lambdaurora.lambdabettergrass.metadata.LBGState;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.io.Resource;
import net.minecraft.resources.io.ResourceManager;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Represents the LambdaBetterGrass resource reloader.
 *
 * @author LambdAurora
 * @version 2.0.0
 * @since 1.4.0
 */
public class LBGResourceReloader {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final LBGLayerTypeManager layerTypeManager;

	public LBGResourceReloader(LBGLayerTypeManager layerTypeManager) {
		this.layerTypeManager = layerTypeManager;
	}

	public void reload(ResourceManager resourceManager) {
		LBGState.reset();
		this.layerTypeManager.load(resourceManager);
		this.loadStates(resourceManager);
	}

	private void loadStates(ResourceManager resourceManager) {
		var blockModelDefinitionContext = new BlockModelDefinition.Context();

		resourceManager.findResources(LBGState.PATH_PREFIX, id -> id.path().endsWith(".json"))
				.forEach((id, resource) -> this.loadState(resourceManager, id, resource, blockModelDefinitionContext));
	}

	/**
	 * Loads the given LambdaBetterGrass state.
	 *
	 * @param resourceManager the resource manager
	 * @param id the resource identifier of the state
	 * @param resource the resource
	 * @param blockModelDefinitionContext the deserialization context of block model definitions
	 */
	private void loadState(
			ResourceManager resourceManager, Identifier id, Resource resource,
			BlockModelDefinition.Context blockModelDefinitionContext
	) {
		var stateId = Identifier.of(
				id.namespace(),
				id.path().substring(LBGState.PATH_PREFIX.length() + 1, id.path().length() - ".json".length())
		);

		var block = BuiltInRegistries.BLOCK.getOptional(stateId);
		if (block.isEmpty()) {
			// The block doesn't exist, so we just ignore the state file.
			return;
		}

		try (var reader = new InputStreamReader(resource.open())) {
			var json = JsonParser.parseReader(reader).getAsJsonObject();
			LBGState.loadMetadataState(stateId, block.get(), resourceManager, json, blockModelDefinitionContext);
		} catch (IOException e) {
			LOGGER.warn("Failed to load LambdaBetterGrass state {}.", stateId, e);
		}
	}
}
