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
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * Represents a metadata for blocks which have snowy variants or equivalent.
 *
 * @author LambdAurora
 * @version 2.2.0
 * @since 1.0.0
 */
public class LBGLayerMetadata {
	public final Identifier id;
	private final LBGLayerType layerType;
	private final boolean layerModel;
	private final @Nullable Vector3f offset;
	private final  BlockStateModel.@Nullable UnbakedRoot variantModels;

	public LBGLayerMetadata(
			Identifier id, LBGLayerType layerType, JsonObject json
	) {
		this.id = id;
		this.layerType = layerType;

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
			var blockModelDefinition = BlockStateModel.Unbaked.CODEC.parse(JsonOps.INSTANCE, json.get("block_state"))
					.getOrThrow(JsonParseException::new);
			this.variantModels = blockModelDefinition.asRoot();
		} else {
			this.variantModels = null;
		}
	}

	/**
	 * {@return the layer type associated with this metadata}
	 */
	public LBGLayerType layerType() {
		return this.layerType;
	}

	public boolean hasLayerModel() {
		return this.layerModel;
	}

	public @Nullable Vector3f offset() {
		return this.offset;
	}

	public LayerUnbakedModels getCustomUnbakedModel(BlockState state) {
		return new LayerUnbakedModels(this.variantModels);
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

	public record LayerUnbakedModels(BlockStateModel.@Nullable UnbakedRoot alternateModel) {
		public boolean isEmpty() {
			return this.alternateModel() == null;
		}
	}
}
