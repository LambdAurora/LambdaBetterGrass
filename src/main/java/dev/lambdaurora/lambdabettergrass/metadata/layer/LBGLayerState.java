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
import net.minecraft.client.renderer.block.model.BlockStateModel;
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
import java.util.stream.Stream;

/**
 * Represents model states, which have layered connection with blocks like snow, with its different {@link LBGLayerMetadata}.
 *
 * @author LambdAurora
 * @version 2.2.0
 * @since 1.0.0
 */
public class LBGLayerState extends LBGState {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final JsonObject DEFAULT_METADATA_LAYER_JSON = new JsonObject();

	static {
		DEFAULT_METADATA_LAYER_JSON.addProperty("layer", true);
	}

	private final Map<BlockState, Map<LBGLayerType, LBGLayerMetadata>> metadatas = new Object2ObjectOpenHashMap<>();

	public LBGLayerState(
			Identifier id, ResourceManager resourceManager, JsonObject json,
			StateDefinition<Block, BlockState> stateDefinition
	) {
		super(id, stateDefinition.getOwner());

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
				LOGGER.warn("Cannot load metadata file \"{}\" from layer state \"{}\" (variant: \"{}\").", metadataId, this.id(), variant, e);
			}
		}
	}

	private void putOrReplaceMetadata(
			String variant, Identifier metadataId, LBGLayerType type, JsonObject metadataJson,
			StateDefinition<Block, BlockState> stateDefinition
	) {
		var metadata = new LBGLayerMetadata(metadataId, type, metadataJson, stateDefinition);

		if (variant.equals("*")) {
			for (var state : stateDefinition.getPossibleStates()) {
				this.putOrReplaceMetadata(state, metadata);
			}
		} else {
			var properties = VariantSelector.extractProperties(stateDefinition, variant);

			for (var state : stateDefinition.getPossibleStates()) {
				if (VariantSelector.match(state, properties)) {
					this.putOrReplaceMetadata(state, metadata);
				}
			}
		}
	}

	private void putOrReplaceMetadata(BlockState state, LBGLayerMetadata metadata) {
		var metadatas = this.metadatas.computeIfAbsent(state, v -> new HashMap<>());
		metadatas.put(metadata.layerType(), metadata);
	}

	public Stream<LBGLayerMetadata> streamMetadata(BlockState state) {
		return this.metadatas.entrySet().stream()
				.filter(entry -> entry.getKey().equals(state))
				.map(Map.Entry::getValue)
				.flatMap(map -> map.values().stream());
	}

	@Override
	public @Nullable BlockStateModel.UnbakedRoot getCustomUnbakedModel(
			BlockState state, BlockStateModel.UnbakedRoot originalModel
	) {
		var metadatas = this.streamMetadata(state)
				.map(metadata -> {
					var models = metadata.getCustomUnbakedModel(state);
					return new LBGCompiledLayerMetadata(metadata.layerType(), metadata.hasLayerModel(), metadata.offset(), models);
				})
				.toList();

		if (!metadatas.isEmpty()) {
			return new LBGLayerUnbakedModel(originalModel, metadatas);
		}

		return null;
	}
}
