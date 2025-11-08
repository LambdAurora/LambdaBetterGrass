/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.resource;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.metadata.layer.LBGLayerType;
import dev.lambdaurora.lambdabettergrass.metadata.layer.LBGLayerTypeData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents a manager of layer types.
 *
 * @author LambdAurora
 * @version 2.0.0
 * @since 2.0.0
 */
public final class LBGLayerTypeManager {
	private static final Logger LOGGER = LoggerFactory.getLogger("LambdaBetterGrass|LBGLayerTypeManager");
	private static final String PREFIX = "bettergrass/layer_types";
	private static final String EXTENSION = ".json";

	private final Map<Identifier, LBGLayerType> types = new HashMap<>();

	/**
	 * Gets a layer type by its identifier.
	 *
	 * @param rawId the raw identifier of the layer type
	 * @return the layer type if present, or {@link Optional#empty()} otherwise
	 * @see #get(Identifier)
	 */
	public Optional<LBGLayerType> get(String rawId) {
		var id = Identifier.parse(rawId);

		return this.get(id).or(() -> {
			if (!rawId.contains(":") && id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
				return this.get(LambdaBetterGrass.id(id.getPath()));
			} else {
				return Optional.empty();
			}
		});
	}

	/**
	 * Gets a layer type by its identifier.
	 *
	 * @param id the identifier of the layer type
	 * @return the layer type if present, or {@link Optional#empty()} otherwise
	 * @see #get(String)
	 */
	public Optional<LBGLayerType> get(Identifier id) {
		return Optional.ofNullable(this.types.get(id));
	}

	public void forEach(Consumer<LBGLayerType> consumer) {
		this.types.values().forEach(consumer);
	}

	public void load(ResourceManager resourceManager) {
		this.types.clear();
		var layerTypes = resourceManager.listResources(PREFIX, path -> path.getPath().endsWith(EXTENSION));
		layerTypes.forEach(this::loadLayerType);
	}

	private void loadLayerType(Identifier resourceId, Resource resource) {
		var id = this.getIdFromResource(resourceId);

		try (var reader = new InputStreamReader(resource.open())) {
			var rawJson = JsonParser.parseReader(reader);
			var loaded = LBGLayerTypeData.CODEC.parse(JsonOps.INSTANCE, rawJson);

			loaded.result().ifPresentOrElse(
					data -> this.types.put(id, new LBGLayerType(id, data)),
					() -> {
						LambdaBetterGrass.warn(LOGGER, "Failed to load layer type \"{}\" due to error: {}",
								id, loaded.error().orElseThrow().message()
						);
					}
			);
		} catch (Exception e) {
			LambdaBetterGrass.warn(LOGGER, "Failed to load layer type \"{}\".", id);
		}
	}

	private Identifier getIdFromResource(Identifier resourceId) {
		final var path = resourceId.getPath();
		return resourceId.withPath(path.substring(PREFIX.length() + 1, path.length() - EXTENSION.length()));
	}
}
