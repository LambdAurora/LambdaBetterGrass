/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.io.StringReader;
import java.util.function.Function;

/**
 * Represents a metadata for blocks which have snowy variants or equivalent.
 *
 * @author LambdAurora
 * @version 1.4.0
 * @since 1.0.0
 */
public class LBGLayerMetadata {
	public final Identifier id;
	public final LBGLayerType layerType;
	private final boolean layerModel;
	private final @Nullable Vector3f offset;
	private final Object2ObjectMap<String, UnbakedModel> variantModels = new Object2ObjectOpenHashMap<>();
	private UnbakedModel alternateModel;
	private final boolean hasAlternateModel;

	public LBGLayerMetadata(Identifier id, @Nullable LBGLayerType layerType, JsonObject json,
			ModelVariantMap.DeserializationContext deserializationContext) {
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

		var map = ModelVariantMap.fromJson(deserializationContext, new StringReader(json.get("block_state").toString()));
		if (map.hasMultipartModel())
			this.alternateModel = map.getMultipartModel();
		else
			this.variantModels.putAll(map.getVariantMap());

		this.hasAlternateModel = true;
	}

	public boolean hasLayerModel() {
		return this.layerModel;
	}

	public @Nullable Vector3f offset() {
		return this.offset;
	}

	public LayerUnbakedModels getCustomUnbakedModel(ModelIdentifier modelId, UnbakedModel originalModel, Function<Identifier, UnbakedModel> modelGetter) {
		UnbakedModel layerModel = null;
		if (this.layerModel) {
			layerModel = this.layerType.getLayerModel(modelGetter);
		}

		UnbakedModel alternateModel = null;
		if (this.hasAlternateModel) {
			if (this.alternateModel != null) {
				alternateModel = this.alternateModel;
			} else {
				UnbakedModel alternateVariantModel = this.variantModels.get(modelId.getVariant());
				if (alternateVariantModel != null) {
					alternateModel = alternateVariantModel;
				}
			}
		}

		return new LayerUnbakedModels(layerModel, alternateModel);
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

	public record LayerUnbakedModels(@Nullable UnbakedModel layerModel, @Nullable UnbakedModel alternateModel) {
		public boolean isEmpty() {
			return this.layerModel() == null && this.alternateModel() == null;
		}
	}
}
