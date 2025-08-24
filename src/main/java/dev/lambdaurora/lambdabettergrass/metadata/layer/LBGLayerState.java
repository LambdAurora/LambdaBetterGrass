/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata.layer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.metadata.LBGState;
import dev.lambdaurora.lambdabettergrass.model.LBGLayerUnbakedModel;
import dev.lambdaurora.lambdabettergrass.util.VariantSelector;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.resources.model.ModelIdentifier;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.io.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Represents model states, which have layered connection with blocks like snow, with its different {@link LBGLayerMetadata}.
 *
 * @author LambdAurora
 * @version 2.1.0
 * @since 1.0.0
 */
public class LBGLayerState extends LBGState {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final JsonObject DEFAULT_METADATA_LAYER_JSON = new JsonObject();

	static {
		DEFAULT_METADATA_LAYER_JSON.addProperty("layer", true);
	}

	private final Map<String, Map<LBGLayerType, LBGLayerMetadata>> metadatas = new Object2ObjectOpenHashMap<>();

	public LBGLayerState(
			Identifier id, ResourceManager resourceManager, JsonObject json,
			StateDefinition<Block, BlockState> stateDefinition
	) {
		super(id);

		if (json.has("variants")) {
			var variants = json.getAsJsonObject("variants");
			variants.entrySet().forEach(entry -> {
				var variant = entry.getValue().getAsJsonObject();
				if (variant.has("data")) {
					this.loadVariant(entry.getKey(), variant, resourceManager, stateDefinition);
				}
			});
		} else if (json.has("data")) {
			this.loadVariant("*", json, resourceManager, stateDefinition);
		} else {
			LOGGER.warn("Invalid state definition for {}, missing data or variants entry.", id);
		}
	}

	private void loadVariant(
			String variant, JsonObject json, ResourceManager resourceManager,
			StateDefinition<Block, BlockState> stateDefinition
	) {
		var metadataId = Identifier.tryParse(json.get("data").getAsString());
		var metadataResourceId = metadataId.withSuffix(".json");

		LambdaBetterGrass.get().layerTypeManager.forEach(type -> {
			this.putOrReplaceMetadata(variant, metadataId, type, DEFAULT_METADATA_LAYER_JSON, stateDefinition);
		});

		var resources = resourceManager.getAllResources(metadataResourceId);
		for (var resource : resources) {
			try (var reader = new InputStreamReader(resource.open())) {
				var metadataJson = JsonParser.parseReader(reader).getAsJsonObject();

				for (var entry : metadataJson.entrySet()) {
					var type = LambdaBetterGrass.get().layerTypeManager.get(entry.getKey());

					if (type.isEmpty())
						continue;

					if (entry.getValue().isJsonObject()) {
						this.putOrReplaceMetadata(
								variant, metadataId, type.get(), entry.getValue().getAsJsonObject(), stateDefinition
						);
					}
				}
			} catch (IOException e) {
				LOGGER.warn("Cannot load metadata file \"{}\" from layer state \"{}\" (variant: \"{}\").", metadataId, id, variant, e);
			}
		}
	}

	private void putOrReplaceMetadata(
			String variant, Identifier metadataId, LBGLayerType type, JsonObject metadataJson,
			StateDefinition<Block, BlockState> stateDefinition
	) {
		var metadatas = this.metadatas.computeIfAbsent(variant, v -> new HashMap<>());
		metadatas.put(type, new LBGLayerMetadata(metadataId, type, metadataJson, stateDefinition));
	}

	public Stream<LBGLayerMetadata> streamMetadata(BlockState state) {
		return this.metadatas.entrySet().stream()
				.filter(entry -> {
					var variant = entry.getKey();
					var properties = VariantSelector.extractProperties(state.getBlock().getStateDefinition(), variant);
					return VariantSelector.match(state, properties);
				})
				.map(Map.Entry::getValue)
				.flatMap(map -> map.values().stream());
	}

	public void forEach(String[] variant, Consumer<LBGLayerMetadata> consumer) {
		this.metadatas.entrySet().stream()
				.filter(entry -> this.matchVariant(variant, entry.getKey().split(",")))
				.flatMap(entry -> entry.getValue().values().stream())
				.forEach(consumer);
	}

	@Override
	public @Nullable UnbakedModel getCustomUnbakedModel(
			ModelIdentifier modelId, UnbakedModel originalModel
	) {
		String[] modelVariant = modelId.variant().split(",");

		for (var entry : this.metadatas.entrySet()) {
			if (entry.getKey().equals("*") || this.matchVariant(modelVariant, entry.getKey().split(","))) {
				var metadatas = entry.getValue().values()
						.stream()
						.map(metadata -> {
							var models = metadata.getCustomUnbakedModel(modelId);
							return new LBGCompiledLayerMetadata(metadata.layerType(), metadata.hasLayerModel(), metadata.offset(), models);
						})
						.toList();

				if (!metadatas.isEmpty()) {
					return new LBGLayerUnbakedModel(originalModel, metadatas);
				}

				return null;
			}
		}
		return null;
	}
}
