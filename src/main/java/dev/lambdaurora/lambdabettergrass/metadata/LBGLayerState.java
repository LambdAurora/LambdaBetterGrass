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
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import dev.lambdaurora.lambdabettergrass.model.LBGLayerUnbakedModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.resources.model.ModelIdentifier;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.io.ResourceManager;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents model states, which have layered connection with blocks like snow, with its different {@link LBGLayerMetadata}.
 *
 * @author LambdAurora
 * @version 1.6.0
 * @since 1.0.0
 */
public class LBGLayerState extends LBGState {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final JsonObject DEFAULT_METADATA_LAYER_JSON = new JsonObject();

	static {
		DEFAULT_METADATA_LAYER_JSON.addProperty("layer", true);
	}

	private final Map<String, List<LBGLayerMetadata>> metadatas = new Object2ObjectOpenHashMap<>();
	private final Block block;

	public LBGLayerState(Identifier id, Block block, ResourceManager resourceManager, JsonObject json,
			BlockModelDefinition.Context deserializationContext) {
		super(id);
		this.block = block;

		deserializationContext.setDefinition(block.getStateDefinition());

		if (json.has("variants")) {
			var variants = json.getAsJsonObject("variants");
			variants.entrySet().forEach(entry -> {
				var variant = entry.getValue().getAsJsonObject();
				if (variant.has("data")) {
					this.loadVariant(entry.getKey(), variant, resourceManager, deserializationContext);
				}
			});
		} else if (json.has("data")) {
			this.loadVariant("*", json, resourceManager, deserializationContext);
		} else {
			LOGGER.warn("Invalid state definition for {}, missing data or variants entry.", id);
		}
	}

	private void loadVariant(String variant, JsonObject json, ResourceManager resourceManager,
			BlockModelDefinition.Context deserializationContext) {
		var metadataId = Identifier.tryParse(json.get("data").getAsString());
		var metadataResourceId = new Identifier(metadataId.namespace(), metadataId.path() + ".json");

		LBGLayerType.forEach(type -> {
			this.putOrReplaceMetadata(variant, metadataId, type, DEFAULT_METADATA_LAYER_JSON, deserializationContext);
		});

		var resources = resourceManager.getAllResources(metadataResourceId);
		for (var resource : resources) {
			try (var reader = new InputStreamReader(resource.open())) {
				var metadataJson = JsonParser.parseReader(reader).getAsJsonObject();

				for (var entry : metadataJson.entrySet()) {
					var type = LBGLayerType.fromName(entry.getKey());

					if (type == null)
						continue;

					if (entry.getValue().isJsonObject()) {
						this.putOrReplaceMetadata(variant, metadataId, type, entry.getValue().getAsJsonObject(), deserializationContext);
					}
				}
			} catch (IOException e) {
				LOGGER.warn("Cannot load metadata file \"{}\" from layer state \"{}\" (variant: \"{}\").", metadataId, id, variant, e);
			}
		}
	}

	private void putOrReplaceMetadata(String variant, Identifier metadataId, LBGLayerType type, JsonObject metadataJson,
			BlockModelDefinition.Context deserializationContext) {
		var metadatas = this.metadatas.computeIfAbsent(variant, v -> new ArrayList<>());
		var it = metadatas.iterator();
		while (it.hasNext()) {
			var next = it.next();

			if (next.layerType == type) {
				it.remove();
				break;
			}
		}

		metadatas.add(new LBGLayerMetadata(metadataId, type, metadataJson, deserializationContext));
	}

	public void forEach(String[] variant, Consumer<LBGLayerMetadata> consumer) {
		for (Map.Entry<String, List<LBGLayerMetadata>> entry : this.metadatas.entrySet()) {
			if (this.matchVariant(variant, entry.getKey().split(","))) {
				entry.getValue().forEach(consumer);
				return;
			}
		}
	}

	@Override
	public @Nullable UnbakedModel getCustomUnbakedModel(ModelIdentifier modelId, UnbakedModel originalModel,
			Function<Identifier, UnbakedModel> modelGetter) {
		String[] modelVariant = modelId.variant().split(",");

		for (var entry : this.metadatas.entrySet()) {
			if (entry.getKey().equals("*") || this.matchVariant(modelVariant, entry.getKey().split(","))) {
				var metadatas = new ArrayList<LBGCompiledLayerMetadata>();

				entry.getValue().forEach(metadata -> {
					var models = metadata.getCustomUnbakedModel(modelId);
					metadatas.add(new LBGCompiledLayerMetadata(metadata.layerType, metadata.offset(), models));
				});

				if (!metadatas.isEmpty()) {
					return new LBGLayerUnbakedModel(originalModel, metadatas);
				}

				return null;
			}
		}
		return null;
	}
}
