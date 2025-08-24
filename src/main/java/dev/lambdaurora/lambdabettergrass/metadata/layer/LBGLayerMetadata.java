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
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.UnbakedBlockStateModel;
import net.minecraft.client.resources.model.ModelIdentifier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.io.StringReader;

/**
 * Represents a metadata for blocks which have snowy variants or equivalent.
 *
 * @author LambdAurora
 * @version 2.0.0
 * @since 1.0.0
 */
public class LBGLayerMetadata {
	public final Identifier id;
	public final LBGLayerType layerType;
	private final boolean layerModel;
	private final @Nullable Vector3f offset;
	/*TODO*/ private final Object2ObjectMap<String, MultiVariant> variantModels = new Object2ObjectOpenHashMap<>();
	private UnbakedBlockStateModel alternateModel;
	private final boolean hasAlternateModel;

	public LBGLayerMetadata(
			Identifier id, @Nullable LBGLayerType layerType, JsonObject json, StateDefinition<Block, BlockState> stateDefinition
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

		if (!json.has("block_state")) {
			this.alternateModel = null;
			this.hasAlternateModel = false;
			return;
		}

		var map = BlockModelDefinition.fromStream(new StringReader(json.get("block_state").toString()));
		map.instantiate(stateDefinition, id.toString());
		if (map.getMultiPart() != null)
			this.alternateModel = map.getMultiPart().instantiate(stateDefinition);
		else {
            /*TODO this.variantModels.put(map.getMultiVariants());*/
        }

		this.hasAlternateModel = true;
	}

	public boolean hasLayerModel() {
		return this.layerModel;
	}

	public @Nullable Vector3f offset() {
		return this.offset;
	}

	public LayerUnbakedModels getCustomUnbakedModel(ModelIdentifier modelId) {
		UnbakedBlockStateModel alternateModel = null;
		if (this.hasAlternateModel) {
			if (this.alternateModel != null) {
				alternateModel = this.alternateModel;
			} else {
				UnbakedBlockStateModel alternateVariantModel = this.variantModels.get(modelId.variant());
				if (alternateVariantModel != null) {
					alternateModel = alternateVariantModel;
				}
			}
		}

		return new LayerUnbakedModels(alternateModel);
	}

	@Override
	public String toString() {
		return "LBGLayerMetadata{" +
				"id=" + this.id +
				", layerType=" + this.layerType +
				", layerModel=" + this.layerModel +
				", hasAlternateModel=" + this.hasAlternateModel +
				'}';
	}

	public record LayerUnbakedModels(@Nullable UnbakedBlockStateModel alternateModel) {
		public boolean isEmpty() {
			return this.alternateModel() == null;
		}
	}
}
