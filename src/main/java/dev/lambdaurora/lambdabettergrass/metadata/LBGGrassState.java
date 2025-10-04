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
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.model.LBGUnbakedModel;
import dev.lambdaurora.lambdabettergrass.resource.LBGContext;
import dev.lambdaurora.lambdabettergrass.util.VariantSelector;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.io.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents grass model states with its different {@link LBGMetadata}.
 *
 * @author LambdAurora
 * @version 2.5.0
 * @since 1.0.0
 */
public class LBGGrassState extends LBGState {
	private static final Logger LOGGER = LoggerFactory.getLogger("LambdaBetterGrass|LBGGrassState");
	private final Map<BlockState, LBGMetadata> metadatas = new Object2ObjectOpenHashMap<>();

	public LBGGrassState(
			@NotNull Identifier id, @NotNull ResourceManager resourceManager, @NotNull JsonObject json,
			@NotNull StateDefinition<Block, BlockState> stateDefinition, @NotNull LBGContext context
	) {
		super(id, stateDefinition.getOwner());

		// Look for variants.
		if (json.has("variants")) {
			record Entry(List<Property.Value<?>> properties, LBGMetadata metadata) {}

			var variants = json.getAsJsonObject("variants")
					.entrySet().stream()
					.map(entry -> {
						var variant = entry.getValue().getAsJsonObject();
						if (variant.has("data")) {
							var metadataId = Identifier.parse(variant.get("data").getAsString());
							var properties = VariantSelector.extractProperties(stateDefinition, entry.getKey());

							return new Entry(properties, this.loadMetadata(resourceManager, context, metadataId));
						} else {
							return null;
						}
					})
					.filter(Objects::nonNull)
					.toList();

			for (var state : stateDefinition.getPossibleStates()) {
				for (var variant : variants) {
					assert variant != null;

					if (VariantSelector.match(state, variant.properties)) {
						this.metadatas.put(state, variant.metadata);
					}
				}
			}

			if (stateDefinition.getProperties().contains(BlockStateProperties.SNOWY)) {
				this.metadatas.forEach((state, metadata) -> {
					if (!state.get(BlockStateProperties.SNOWY)) {
						var snowyState = state.with(BlockStateProperties.SNOWY, true);
						var snowyMetadata = this.metadatas.get(snowyState);

						if (snowyMetadata != null) {
							snowyMetadata.snowyModelVariantProvider = bakedModel -> metadata.snowyModelVariant = bakedModel;
						}
					}
				});
			}
		} else if (json.has("data")) { // Look for a common metadata if no variants are specified.
			var metadataId = Identifier.parse(json.get("data").getAsString());
			var metadata = this.loadMetadata(resourceManager, context, metadataId);
			for (var state : stateDefinition.getPossibleStates()) {
				this.metadatas.put(state, metadata);
			}
		} // The state file is invalid, cannot find any metadata.
	}

	/**
	 * Loads the metadata from the resource manager.
	 *
	 * @param resourceManager the resource manager
	 * @param context the LambdaBetterGrass context
	 * @param metadataId the metadata identifier
	 * @return the metadata if loaded successfully, else {@code null}
	 */
	private @Nullable LBGMetadata loadMetadata(
			@NotNull ResourceManager resourceManager, @NotNull LBGContext context, @NotNull Identifier metadataId
	) {
		var metadataResourceId = metadataId.withSuffix(".json");
		try (var reader = new InputStreamReader(resourceManager.getResourceOrThrow(metadataResourceId).open())) {
			var metadataJson = JsonParser.parseReader(reader).getAsJsonObject();

			return new LBGMetadata(resourceManager, context, metadataId, metadataJson);
		} catch (Exception e) {
			LambdaBetterGrass.warn(LOGGER, "Could not load metadata `{}`.", metadataId, e);
		}
		return null;
	}

	/**
	 * Returns the metadata corresponding to the specified block state.
	 *
	 * @param state the block state
	 * @return a metadata if it exists for the given block state, else {@code null}
	 */
	public @Nullable LBGMetadata getMetadata(@NotNull BlockState state) {
		return this.metadatas.get(state);
	}

	@Override
	public @Nullable BlockStateModel.UnbakedRoot getCustomUnbakedModel(
			BlockState state, BlockStateModel.UnbakedRoot originalModel
	) {
		var metadata = this.getMetadata(state);
		if (metadata != null) {
			return new LBGUnbakedModel(originalModel, metadata);
		}
		return null;
	}
}
