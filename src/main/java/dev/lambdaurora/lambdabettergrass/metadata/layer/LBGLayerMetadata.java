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
import dev.lambdaurora.lambdabettergrass.util.VariantSelector;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.UnbakedBlockStateModel;
import net.minecraft.client.resources.model.ModelIdentifier;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.io.StringReader;
import java.util.Map;

/**
 * Represents a metadata for blocks which have snowy variants or equivalent.
 *
 * @author LambdAurora
 * @version 2.1.0
 * @since 1.0.0
 */
public class LBGLayerMetadata {
	public final Identifier id;
	private final LBGLayerType layerType;
	private final StateDefinition<Block, BlockState> stateDefinition;
	private final boolean layerModel;
	private final @Nullable Vector3f offset;
	private final Map<BlockState, UnbakedBlockStateModel> variantModels;

	public LBGLayerMetadata(
			Identifier id, @Nullable LBGLayerType layerType, JsonObject json,
			StateDefinition<Block, BlockState> stateDefinition
	) {
		this.id = id;
		this.layerType = layerType;
		this.stateDefinition = stateDefinition;

		if (json.has("layer")) {
			this.layerModel = json.get("layer").getAsBoolean();
		} else {
			this.layerModel = false;
		}

		if (json.has("offset")) {
			var offsetJson = json.get("offset");
			if (offsetJson.isJsonArray()) {
				var offsetArray = offsetJson.getAsJsonArray();
				this.offset = new Vector3f(
						offsetArray.get(0).getAsFloat(), offsetArray.get(1).getAsFloat(), offsetArray.get(2).getAsFloat()
				);
			} else this.offset = null;
		} else this.offset = null;

		if (json.has("block_state")) {
			var blockModelDefinition = BlockModelDefinition.fromStream(new StringReader(json.get("block_state").toString()));
			this.variantModels = blockModelDefinition.instantiate(stateDefinition, id.toString());
		} else {
			this.variantModels = null;
		}
	}

	/**
	 * {@return the layer type associated with this metadata}
	 */
	public @NotNull LBGLayerType layerType() {
		return this.layerType;
	}

	public boolean hasLayerModel() {
		return this.layerModel;
	}

	public @Nullable Vector3f offset() {
		return this.offset;
	}

	public LayerUnbakedModels getCustomUnbakedModel(ModelIdentifier modelId) {
		if (this.variantModels == null || modelId.variant().equals(ModelIdentifier.INVENTORY_VARIANT))
			return new LayerUnbakedModels(null);

		var properties = VariantSelector.extractProperties(this.stateDefinition, modelId.variant());

		var state = this.stateDefinition.getOwner().defaultState();
		for (var property : properties) {
			state = this.withValue(state, property);
		}

		return new LayerUnbakedModels(this.variantModels.get(state));
	}

	private <T extends Comparable<T>> Property.Value<T> makeValue(Property<T> property, String rawValue) {
		var value = property.getValue(rawValue);
		return value.map(property::value).orElse(null);
	}

	private <T extends Comparable<T>> BlockState withValue(BlockState state, Property.Value<T> value) {
		return state.with(value.property(), value.value());
	}

	@Override
	public String toString() {
		return "LBGLayerMetadata{" +
				"id=" + this.id +
				", layerType=" + this.layerType +
				", layerModel=" + this.layerModel +
				", variantModels=" + this.variantModels +
				'}';
	}

	public record LayerUnbakedModels(@Nullable UnbakedModel alternateModel) {
		public boolean isEmpty() {
			return this.alternateModel() == null;
		}
	}
}
